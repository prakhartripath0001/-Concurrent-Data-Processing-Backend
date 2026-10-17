package com.Data_Processing_Backend.imports;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "import_jobs")
public class ImportJob {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ProcessingMode mode;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private JobStatus status;
    @Column(nullable = false) private long totalRecords;
    @Column(nullable = false) private long successfulRecords;
    @Column(nullable = false) private long failedRecords;
    @Column(nullable = false) private long duplicateRecords;
    @Column(nullable = false) private long durationMillis;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    @Lob private String errors;
    protected ImportJob() {}
    public ImportJob(ProcessingMode mode) { this.mode = mode; this.status = JobStatus.RUNNING; this.errors = ""; }
    public String getId() { return id; } public ProcessingMode getMode() { return mode; } public JobStatus getStatus() { return status; }
    public long getTotalRecords() { return totalRecords; } public long getSuccessfulRecords() { return successfulRecords; } public long getFailedRecords() { return failedRecords; }
    public long getDuplicateRecords() { return duplicateRecords; } public long getDurationMillis() { return durationMillis; } public Instant getCreatedAt() { return createdAt; } public String getErrors() { return errors; }
    public void complete(ImportResult result) { status = JobStatus.COMPLETED; totalRecords = result.total(); successfulRecords = result.valid(); failedRecords = result.invalid(); duplicateRecords = result.duplicates(); durationMillis = result.durationMillis(); errors = result.errors(); }
    public void fail(String error) { status = JobStatus.FAILED; errors = error; }
}
