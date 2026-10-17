package com.Data_Processing_Backend.imports;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
public class ImportController {
    private final CsvImportService service; private final ImportJobRepository jobs;
    public ImportController(CsvImportService service, ImportJobRepository jobs) { this.service = service; this.jobs = jobs; }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportJob> upload(@RequestPart MultipartFile file, @RequestParam(defaultValue = "PARALLEL") ProcessingMode mode) {
        ImportJob job = jobs.save(new ImportJob(mode));
        try { job.complete(service.process(file, mode)); } catch (RuntimeException ex) { job.fail(ex.getMessage()); }
        jobs.save(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }
    @GetMapping("/{id}") public ImportJob get(@PathVariable String id) { return jobs.findById(id).orElseThrow(() -> new IllegalArgumentException("Import job not found")); }
}
