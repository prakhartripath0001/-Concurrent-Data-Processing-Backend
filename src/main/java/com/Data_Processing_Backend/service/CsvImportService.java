package com.Data_Processing_Backend.service;

import com.Data_Processing_Backend.config.ImportProperties;
import com.Data_Processing_Backend.dto.CsvRecord;
import com.Data_Processing_Backend.dto.ImportResult;
import com.Data_Processing_Backend.entity.ProcessingMode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@Service
public class CsvImportService {

    private static final int MAX_REPORTED_ERRORS = 1_000;
    private final ImportWorker worker;
    private final ImportProperties properties;

    public CsvImportService(ImportWorker worker, ImportProperties properties) {
        this.worker = worker;
        this.properties = properties;
    }

    public ImportResult process(MultipartFile file, ProcessingMode mode) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty");
        }
        long started = System.nanoTime();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || !header.replace(" ", "").equalsIgnoreCase("entryCode,name,batchNumber,quantity,price,expiryDate")) {
                throw new IllegalArgumentException("CSV header must be entryCode,name,batchNumber,quantity,price,expiryDate");
            }
            return mode == ProcessingMode.SEQUENTIAL ? sequential(reader, started) : parallel(reader, started);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot read CSV file", ex);
        }
    }

    private ImportResult sequential(BufferedReader reader, long started) throws IOException {
        Counters counters = new Counters();
        Set<String> keys = new HashSet<>();
        String line;
        long row = 1;
        while ((line = reader.readLine()) != null) {
            row++;
            processLine(line, row, keys, counters);
        }
        return counters.result(started);
    }

    private ImportResult parallel(BufferedReader reader, long started) throws IOException {
        Counters counters = new Counters();
        Set<String> keys = ConcurrentHashMap.newKeySet();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                properties.parallelism(),
                properties.parallelism(),
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(properties.queueCapacity()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        String line;
        long row = 1;
        try {
            while ((line = reader.readLine()) != null) {
                row++;
                final String taskLine = line;
                final long taskRow = row;
                executor.execute(() -> processLine(taskLine, taskRow, keys, counters));
            }
        } finally {
            executor.shutdown();
        }
        try {
            if (!executor.awaitTermination(30, TimeUnit.MINUTES)) {
                throw new IllegalStateException("Import timed out");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Import interrupted", ex);
        }
        return counters.result(started);
    }

    private void processLine(String line, long row, Set<String> keys, Counters counters) {
        counters.total.increment();
        try {
            CsvRecord record = CsvRowValidator.parse(line);
            String key = record.code() + "\u0000" + record.batch();
            if (!keys.add(key) || !worker.insert(record)) {
                counters.duplicates.increment();
            } else {
                counters.valid.increment();
            }
        } catch (IllegalArgumentException ex) {
            counters.invalid.increment();
            counters.addError(row + ": " + ex.getMessage());
        } catch (RuntimeException ex) {
            counters.invalid.increment();
            counters.addError(row + ": database error");
        }
    }

    private static final class Counters {
        private final LongAdder total = new LongAdder();
        private final LongAdder valid = new LongAdder();
        private final LongAdder invalid = new LongAdder();
        private final LongAdder duplicates = new LongAdder();
        private final List<String> errors = Collections.synchronizedList(new ArrayList<>());

        void addError(String error) {
            if (errors.size() < MAX_REPORTED_ERRORS) {
                errors.add(error);
            }
        }

        ImportResult result(long started) {
            return new ImportResult(
                    total.sum(),
                    valid.sum(),
                    invalid.sum(),
                    duplicates.sum(),
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started),
                    String.join("\n", errors)
            );
        }
    }
}
