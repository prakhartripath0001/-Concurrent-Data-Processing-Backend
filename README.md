# Inventory CSV Data Processing Backend

A production-grade Java 21 and Spring Boot service built for high-throughput, concurrent CSV data ingestion, validation, and inventory management. The unique business key is defined by entry code + batch number (`entry_code` + `batch_number`).

---

## Architecture and Project Structure

The codebase is organized into a clean layered architecture under `com.Data_Processing_Backend`:

```text
com.Data_Processing_Backend
├── controller/                      # REST API Layer
│   ├── ImportController.java        # CSV upload and job status tracking
│   └── InventoryController.java     # Inventory CRUD and search endpoints
├── service/                         # Business Logic Layer
│   ├── CsvImportService.java        # Sequential and parallel streaming CSV processing
│   ├── ImportWorker.java            # Transactional row persistence worker
│   ├── CsvRowValidator.java         # CSV parser and strict business validator
│   └── InventoryService.java        # Inventory business rules, sorting and pagination
├── repository/                      # Data Access Layer (Spring Data JPA)
│   ├── ImportJobRepository.java     # Persistence repository for import jobs
│   └── InventoryRepository.java     # Persistence repository for inventory items
├── entity/                          # Domain Model and Persistence
│   ├── ImportJob.java               # JPA Entity for tracking import job executions
│   ├── Inventory.java               # JPA Entity for inventory records
│   └── enums/                       # Domain Enums
│       ├── JobStatus.java           # RUNNING, COMPLETED, FAILED
│       └── ProcessingMode.java      # SEQUENTIAL, PARALLEL
├── dto/                             # Data Transfer Objects
│   ├── CsvRecord.java               # Immutable record of parsed row fields
│   ├── ImportResult.java            # Summary metrics of an import execution
│   └── InventoryRequest.java        # Validated input payload for inventory operations
├── exception/                       # Custom Domain Exceptions
│   ├── DuplicateInventoryException.java
│   └── InventoryNotFoundException.java
├── config/                          # Configuration Properties
│   └── ImportProperties.java        # Thread pool parallelism and queue capacity
└── common/                          # Cross-cutting Concerns
    └── ApiExceptionHandler.java     # Global controller advice (@RestControllerAdvice)
```

---

## Prerequisites

Before running the application locally, ensure you have:

* **Java Development Kit (JDK) 21** or later installed.
  * Verify via terminal:
    ```bash
    java -version
    ```
* **Git** installed.
* **Maven**: Not required separately. The bundled Maven Wrapper (`mvnw` / `mvnw.cmd`) is used.
* **Database**: Runs out-of-the-box with an embedded **H2 in-memory database** (no PostgreSQL or MySQL setup required).

---

## How to Run Locally

### 1. Clone and Navigate to Repository

```bash
git clone <repository-url>
cd Data-Processing-Backend
```

### 2. Run Using the Maven Wrapper

#### On Windows (PowerShell or Command Prompt):
```powershell
.\mvnw.cmd spring-boot:run
```

#### On macOS / Linux:
```bash
chmod +x ./mvnw
./mvnw spring-boot:run
```

Once started, the application will be accessible at:
```text
http://localhost:8080
```

---

### 3. Alternative: Build and Run Executable JAR

You can package and execute the standalone JAR file:

#### On Windows:
```powershell
# Build the JAR (skipping tests for speed)
.\mvnw.cmd clean package -DskipTests

# Run the packaged JAR
java -jar target/Data-Processing-Backend-0.0.1-SNAPSHOT.jar
```

#### On macOS / Linux:
```bash
./mvnw clean package -DskipTests
java -jar target/Data-Processing-Backend-0.0.1-SNAPSHOT.jar
```

---

## Running Automated Tests

To execute all unit and integration tests:

#### Windows:
```powershell
.\mvnw.cmd test
```

#### macOS / Linux:
```bash
./mvnw test
```

---

## Generating Sample Test Data

A helper script is included to generate 10,000 valid sample inventory records for testing and benchmarking.

#### On Windows (PowerShell):
```powershell
.\tools\generate-sample-csv.ps1
```
This generates `sample-data/inventory-10000.csv`.

---

## REST API Reference and Examples

### 1. CSV Import APIs

#### Upload CSV (`POST /api/imports`)
Supports `SEQUENTIAL` (default baseline) or `PARALLEL` (multi-threaded executor).

* **PowerShell / Windows:**
  ```powershell
  curl.exe -F "file=@sample-data/inventory-10000.csv" "http://localhost:8080/api/imports?mode=PARALLEL"
  ```
* **Bash / Linux / macOS:**
  ```bash
  curl -F "file=@sample-data/inventory-10000.csv" "http://localhost:8080/api/imports?mode=PARALLEL"
  ```

* **Sample Response (201 Created):**
  ```json
  {
    "id": "7f9c2d1b-3f2e-4b67-9c88-123456789abc",
    "mode": "PARALLEL",
    "status": "COMPLETED",
    "totalRecords": 10000,
    "successfulRecords": 10000,
    "failedRecords": 0,
    "duplicateRecords": 0,
    "durationMillis": 1284,
    "createdAt": "2026-10-17T13:30:00Z",
    "errors": ""
  }
  ```

#### Get Import Job Details (`GET /api/imports/{id}`)
```bash
curl http://localhost:8080/api/imports/7f9c2d1b-3f2e-4b67-9c88-123456789abc
```

---

### 2. Inventory Management APIs

#### Create Inventory Record (`POST /api/inventory`)
```bash
curl -X POST http://localhost:8080/api/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "entryCode": "MED-00001",
    "name": "Amoxicillin 500mg",
    "batchNumber": "BATCH-00001",
    "quantity": 100,
    "price": 14.50,
    "expiryDate": "2030-12-31"
  }'
```

#### Get Inventory Record by ID (`GET /api/inventory/{id}`)
```bash
curl http://localhost:8080/api/inventory/1
```

#### Search and Paginate Inventory (`GET /api/inventory`)
Search query `q` matches across `entryCode`, `name`, or `batchNumber`.
```bash
curl "http://localhost:8080/api/inventory?q=Amoxicillin&page=0&size=20&sort=price&direction=asc"
```

#### Update Inventory Record (`PUT /api/inventory/{id}`)
Updates mutable fields (`name`, `quantity`, `price`, `expiryDate`). Entry code and batch number are immutable.
```bash
curl -X PUT http://localhost:8080/api/inventory/1 \
  -H "Content-Type: application/json" \
  -d '{
    "entryCode": "MED-00001",
    "name": "Amoxicillin 500mg (Updated)",
    "batchNumber": "BATCH-00001",
    "quantity": 150,
    "price": 16.00,
    "expiryDate": "2031-06-30"
  }'
```

---

## CSV File Specification

The uploaded CSV file must adhere to the following specification:

### Header format:
```text
entryCode,name,batchNumber,quantity,price,expiryDate
```

### Validation rules:
| Column | Type | Validation Rules |
| :--- | :--- | :--- |
| `entryCode` | String | Required, non-blank |
| `name` | String | Required, non-blank |
| `batchNumber` | String | Required, non-blank |
| `quantity` | Integer | Required, non-negative (>= 0) |
| `price` | Decimal | Required, strictly positive (> 0.00) |
| `expiryDate` | Date | Required, ISO format (`yyyy-MM-dd`), must be in the **future** |

* Quoted values and escaped quotes (`"value, with comma"`, `"escaped ""quote"""`) are properly parsed.
* Rows with invalid formats or validation failures are recorded in the job error report (up to 1,000 row errors) without aborting valid row transactions.
