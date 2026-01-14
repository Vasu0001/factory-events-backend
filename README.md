# Factory Events Backend (Spring Boot)

## Overview

This project implements a backend system for ingesting and analyzing machine events in a factory environment.

Machines continuously emit events containing operational details such as duration and defect counts.  
The backend is responsible for:

- Receiving and storing events reliably
- Deduplicating and updating events correctly
- Providing time-window based statistics
- Remaining thread-safe under concurrent ingestion

This project is implemented as part of a **Backend Intern Assignment** using **Spring Boot and JPA**.

---

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot
- **ORM:** Spring Data JPA (Hibernate)
- **Database:** PostgreSQL (local) / H2 (tests)
- **Build Tool:** Maven
- **Testing:** JUnit 5, Spring Boot Test

---

## Architecture

**Controller → Service → Repository → Database**


### Responsibilities

- **Controllers**: Handle HTTP requests and responses
- **Service**: Validation, deduplication, updates, and stats calculation
- **Repository**: Database access using JPA
- **Entity**: Database schema representation
- **DTOs**: Request and response payloads

---

## Data Model

### EventEntity (`events` table)

| Field        | Description                            |
| ------------ | -------------------------------------- |
| eventId (PK) | Unique identifier for each event       |
| eventTime    | Logical time of the event              |
| receivedTime | Time when backend processed the event  |
| factoryId    | Factory identifier                     |
| lineId       | Production line identifier             |
| machineId    | Machine identifier                     |
| durationMs   | Duration of operation                  |
| defectCount  | Number of defects (`-1` means unknown) |
| payloadHash  | Hash used for deduplication            |

---

## Event Ingestion Logic

### Validation Rules

An event is **rejected** if:

- `durationMs < 0` or `durationMs > 6 hours`
- `eventTime` is more than **15 minutes in the future**

Rejected events are returned with rejection reasons.

---

### Deduplication & Update Rules

Events are deduplicated using **eventId**.

| Scenario                                              | Outcome |
| ----------------------------------------------------- | ------- |
| Same eventId + identical payload                      | Deduped |
| Same eventId + different payload + newer receivedTime | Updated |
| Same eventId + different payload + older receivedTime | Ignored |

---


### Payload Comparison

A lightweight payload hash is computed using:

eventId | factoryId | lineId | machineId | durationMs | defectCount

This avoids deep object comparison and improves ingestion performance.

## Thread Safety

Thread safety is achieved using:

1. `@Transactional` boundaries in the service layer
2. Database primary key constraint on `event_id`
3. Idempotent update logic
4. No shared mutable in-memory state

Concurrent ingestion of the same eventId does not create duplicates.

---

## Query Endpoints

### 1. Batch Ingestion

**POST /events/batch**

Accepts a list of events and returns:

- accepted
- updated
- deduped
- rejected
- ignored
- rejection reasons

---

### 2. Machine Statistics

**GET /stats?machineId=&start=&end=**

- `start` is inclusive
- `end` is exclusive

Returns:

- `eventsCount`
- `defectsCount` (ignores `defectCount = -1`)
- `avgDefectRate` (defects per hour)
- `status`
  - `Healthy` if avgDefectRate < 2.0
  - `Warning` otherwise

---

### 3. Top Defect Lines

**GET /stats/top-defect-lines?factoryId=&from=&to=&limit=**

Returns top production lines sorted by total defects within a given time window.


---

## Performance Strategy

- In-memory batch deduplication using `HashMap`
- Reduced database calls per batch
- Single transaction per batch
- Stream-based aggregation for analytics

The system comfortably processes **1000 events in under 1 second** on a standard laptop.

---

## Tests

The test suite covers:

1. Duplicate event deduplication
2. Event update on newer payload
3. Ignore older conflicting updates
4. Invalid duration rejection
5. Future eventTime rejection
6. Ignoring `defectCount = -1` in stats
7. Start-inclusive / end-exclusive correctness
8. Thread-safety under concurrent ingestion

---

## Setup Guide

### 1. Clone the Repository

Clone the repository to your local machine.

```bash
git clone [https://github.com/Vasu0001/factory-events-backend]

```

## 2. Build the Project

Navigate to the project root directory and run the following Maven command to build the project:

```bash
mvn clean install
```

This will download the necessary dependencies and compile the project.

### 3. Run the Application

After the build is successful, you can run the application using the following command:

```bash
mvn spring-boot:run
```

