package com.Data_Processing_Backend.entity;

import com.Data_Processing_Backend.dto.ImportResult;
import com.Data_Processing_Backend.entity.enums.JobStatus;
import com.Data_Processing_Backend.entity.enums.ProcessingMode;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "import_jobs")
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(nullable = false)
    private long totalRecords;

    @Column(nullable = false)
    private long successfulRecords;

    @Column(nullable = false)
    private long failedRecords;

    @Column(nullable = false)
    private long duplicateRecords;

    @Column(nullable = false)
    private long durationMillis;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Lob
    private String errors;

    protected ImportJob() {}

    public ImportJob(ProcessingMode mode) {
        this.mode = mode;
        this.status = JobStatus.RUNNING;
        this.errors = "";
    }

    public String getId() { return id; }
    public ProcessingMode getMode() { return mode; }
    public JobStatus getStatus() { return status; }
    public long getTotalRecords() { return totalRecords; }
    public long getSuccessfulRecords() { return successfulRecords; }
    public long getFailedRecords() { return failedRecords; }
    public long getDuplicateRecords() { return duplicateRecords; }
    public long getDurationMillis() { return durationMillis; }
    public Instant getCreatedAt() { return createdAt; }
    public String getErrors() { return errors; }

    public void complete(ImportResult result) {
        this.status = JobStatus.COMPLETED;
        this.totalRecords = result.total();
        this.successfulRecords = result.valid();
        this.failedRecords = result.invalid();
        this.duplicateRecords = result.duplicates();
        this.durationMillis = result.durationMillis();
        this.errors = result.errors();
    }

    public void fail(String error) {
        this.status = JobStatus.FAILED;
        this.errors = error;
    }
}
