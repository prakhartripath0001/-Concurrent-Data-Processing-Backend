# Inventory CSV Processing Service

Java 21 / Spring Boot service for safely importing large inventory CSV files. The business key is **entry code + batch number**. The default database is embedded H2, so the application runs without external infrastructure.

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

Generate the required 10,000-row dataset:

```powershell
.\tools\generate-sample-csv.ps1
```

Run tests:

```powershell
.\mvnw.cmd test
```

## API

* `POST /api/imports?mode=SEQUENTIAL|PARALLEL` — multipart form field `file`; returns an import job and its report.
* `GET /api/imports/{id}` — retrieves mode, status, record totals, duration, and row-level validation errors.
* `POST /api/inventory` — creates a record.
* `GET /api/inventory/{id}` — retrieves a record.
* `PUT /api/inventory/{id}` — updates its mutable fields.
* `GET /api/inventory?q=aspirin&page=0&size=20&sort=name&direction=asc` — searches code, name, or batch.

Expected CSV header:

```text
entryCode,name,batchNumber,quantity,price,expiryDate
```

Dates use ISO format (`yyyy-MM-dd`) and must be future dates. Quantities must be non-negative and prices strictly positive. Quoted CSV fields and escaped quotes are accepted.

## Processing and reliability

The importer streams from the multipart input with a buffered reader; it does not materialize all CSV rows. Sequential mode is the correctness baseline. Parallel mode uses a fixed-size bounded `ThreadPoolExecutor` with an `ArrayBlockingQueue`; `CallerRunsPolicy` applies back-pressure to the file-reading thread. Atomic counters and a concurrent business-key set protect shared import state. The database unique constraint remains the final duplicate guard under concurrent requests.

Validation errors are retained with CSV row numbers (up to 1,000 per job). Import status is always persisted as `COMPLETED` or `FAILED`, including malformed headers or unreadable uploads.

## Benchmark protocol

Generate the dataset and invoke each mode against a fresh database:

```powershell
curl.exe -F "file=@sample-data/inventory-10000.csv" "http://localhost:8080/api/imports?mode=SEQUENTIAL"
curl.exe -F "file=@sample-data/inventory-10000.csv" "http://localhost:8080/api/imports?mode=PARALLEL"
```

The response reports total, valid, invalid, duplicate, and `durationMillis`. Throughput is `totalRecords / (durationMillis / 1000)`. Run the two requests in separate process/database runs to keep duplicate checks comparable. Measure peak heap externally with Java Flight Recorder or VisualVM. Do not presume parallel mode is faster: database round trips and a small CPU core count can make sequential processing win.
