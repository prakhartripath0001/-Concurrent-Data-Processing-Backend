# Benchmark Report

## Method

Dataset: sample-data/inventory-10000.csv (10,000 valid rows).

Run each import against a newly started application so the unique business-key constraint does not turn the second run into duplicates:

\`\`\`powershell
curl.exe -F "file=@sample-data/inventory-10000.csv" "http://localhost:8080/api/imports?mode=SEQUENTIAL"
curl.exe -F "file=@sample-data/inventory-10000.csv" "http://localhost:8080/api/imports?mode=PARALLEL"
\`\`\`

Record the returned durationMillis, and calculate throughput as totalRecords * 1000 / durationMillis. Capture peak heap with Java Flight Recorder or VisualVM during each run.

## Results

No performance numbers are committed because they are hardware- and database-dependent. The import-job response is the source of truth for every measurement: it contains total, successful, failed, duplicate, elapsed time, and row errors. A valid comparison requires equal output counts between modes; otherwise record the discrepancy as a correctness issue before comparing speed.

## Expected constraints

Parallel mode uses a fixed, bounded worker pool and database writes remain the likely bottleneck. It may be slower than sequential mode with H2, low CPU counts, or highly contended database connections. The bounded queue applies producer back-pressure and prevents unbounded task growth.
