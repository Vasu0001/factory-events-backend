# BENCHMARK.md

## System Specifications

- **Processor:** AMD Ryzen 5 3450U with Radeon Vega Mobile Gfx (2.10 GHz)
- **RAM:** 8.00 GB (5.89 GB usable)
- **Operating System:** Windows 10
- **Java Version:** OpenJDK 17
- **Database:** H2 (in-memory for tests) / PostgreSQL (local supported)

---

## Benchmark Objective

Measure the time taken to ingest **one batch of 1000 events** using the batch ingestion logic (`/events/batch`).

The goal is to verify that the system can process 1000 events **in under 1 second**, as required by the assignment.

---

## Benchmark Methodology

- A batch of 1000 valid events is generated in memory.
- The batch is processed through the service layer (`EventService.processBatch`).
- All validation, deduplication, update logic, and persistence occur inside a single transaction.
- No external caching, message queues, or asynchronous processing are used.
- The database runs locally (no network latency).

---

## Benchmark Command

```bash
mvn test -Dtest=EventServiceTest

```

## Measured Results

| Batch Size  | Time Taken  |
| ----------- | ----------- |
| 1000 events | ~180–300 ms |

## Optimizations Applied

- In-memory batch deduplication using HashMap
- Reduced database writes by resolving duplicates before persistence
- Primary key–based deduplication at the database level
- Single transaction per batch ingestion
