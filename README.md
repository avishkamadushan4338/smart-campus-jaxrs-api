# Smart Campus JAX-RS API

## API Overview

This is a RESTful API for managing rooms and IoT sensors in a smart campus environment. It is built using **JAX-RS (Jersey 2.41)** with an **embedded Jetty 9** server and packaged as a Maven project. No database is used — all data is held in thread-safe in-memory structures (`ConcurrentHashMap`, `CopyOnWriteArrayList`).

The base URL once the server is running is: `http://localhost:8080/api/v1`

There are two main resources — **rooms** and **sensors** — plus a nested readings sub-resource under each sensor. A quick summary of what is available:

- `GET /api/v1/` — discovery endpoint, returns API metadata and resource links
- `GET /api/v1/rooms` — list all rooms
- `POST /api/v1/rooms` — create a new room
- `GET /api/v1/rooms/{roomId}` — get a single room by ID
- `DELETE /api/v1/rooms/{roomId}` — delete a room (blocked if sensors are still assigned)
- `GET /api/v1/sensors` — list all sensors, with an optional `?type=` filter
- `POST /api/v1/sensors` — register a new sensor (must include a valid `roomId`)
- `GET /api/v1/sensors/{sensorId}` — get a single sensor by ID
- `GET /api/v1/sensors/{sensorId}/readings` — get all historical readings for a sensor
- `POST /api/v1/sensors/{sensorId}/readings` — add a new reading (blocked if sensor is in MAINTENANCE)

Key design notes:

- Data is stored in `InMemoryStore`, a singleton backed by `ConcurrentHashMap` and `CopyOnWriteArrayList`. This is shared across all requests because JAX-RS creates a new resource class instance per request, so instance variables do not persist.
- Each domain error has its own custom exception and exception mapper, so the API always returns a structured JSON error body — never a raw stack trace.
- `GlobalExceptionMapper` is the catch-all safety net, returning a generic `500` for any unexpected runtime error without leaking internal details.
- `ApiLoggingFilter` logs every incoming request and outgoing response at the application boundary, even requests that never reach a resource method.
- Sensor readings live under `/sensors/{sensorId}/readings` via the Sub-Resource Locator pattern, keeping telemetry logic in its own dedicated class.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 8 |
| REST framework | JAX-RS 2.1 (Jersey 2.41) |
| JSON serialisation | Jackson (via `jersey-media-json-jackson`) |
| Servlet container | Embedded Jetty 9 (via `jetty-maven-plugin`) |
| Build tool | Apache Maven |
| Storage | Thread-safe in-memory (`ConcurrentHashMap`, `CopyOnWriteArrayList`) |

---

## Project Structure

```
src/main/java/com/smartcampus/
├── config/
│   └── SmartCampusApplication.java   — Jersey ResourceConfig entry point
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   ├── SensorReading.java
│   ├── ApiError.java
│   └── DiscoveryResponse.java
├── resource/
│   ├── DiscoveryResource.java        — GET /api/v1/
│   ├── RoomResource.java             — GET/POST/DELETE /api/v1/rooms[/{id}]
│   ├── SensorResource.java           — GET/POST /api/v1/sensors[/{id}]
│   └── SensorReadingResource.java    — Sub-resource: /sensors/{id}/readings
├── store/
│   └── InMemoryStore.java            — Singleton thread-safe data store
├── exception/
│   ├── RoomNotEmptyException.java
│   ├── LinkedResourceNotFoundException.java
│   └── SensorUnavailableException.java
├── mapper/
│   ├── RoomNotEmptyExceptionMapper.java           — 409 Conflict
│   ├── LinkedResourceNotFoundExceptionMapper.java — 422 Unprocessable Entity
│   ├── SensorUnavailableExceptionMapper.java      — 403 Forbidden
│   └── GlobalExceptionMapper.java                 — 500 Internal Server Error
├── filter/
│   └── ApiLoggingFilter.java         — Request/response logging
└── util/
    └── SeedData.java                 — Demo data loaded at startup
```

---

## How to Build and Run

### Prerequisites

- **JDK 8 or later** — verify with `java -version`
- **Maven 3.6 or later** — verify with `mvn -version`

### Start the embedded server

```bash
mvn jetty:run
```

The server starts on **http://localhost:8080**. Seed data (3 rooms, 4 sensors) is loaded automatically so all endpoints are immediately testable.

### Build a deployable WAR

```bash
mvn clean package
```

The WAR is written to `target/smart-campus-jaxrs-api-1.0-SNAPSHOT.war`. Deploy to any Servlet 3.1-compatible container (e.g. Tomcat 9) by copying it to the `webapps/` directory.

### Stop the server

Press **Ctrl+C** in the terminal running `mvn jetty:run`.

---

## Endpoint Summary

### Discovery

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/` | 200 | API metadata — version, admin contact, resource links |

### Rooms

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/rooms` | 200 | List all rooms |
| POST | `/api/v1/rooms` | 201 / 400 / 409 | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | 200 / 404 | Get a room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | 200 / 404 / 409 | Delete a room (blocked if sensors assigned) |

### Sensors

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/sensors` | 200 | List all sensors (optional `?type=` filter) |
| POST | `/api/v1/sensors` | 201 / 400 / 409 / 422 | Create a sensor linked to an existing room |
| GET | `/api/v1/sensors/{sensorId}` | 200 / 404 | Get a sensor by ID |

### Sensor Readings (Sub-Resource)

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | 200 / 404 | List all readings for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | 201 / 400 / 403 / 404 | Record a new reading |

---

## Sample curl Commands

> Seed data is pre-loaded at startup so these commands work immediately after `mvn jetty:run`.

**1. Discover the API**
```bash
curl -s http://localhost:8080/api/v1/ | python -m json.tool
```

**2. List all rooms**
```bash
curl -s http://localhost:8080/api/v1/rooms | python -m json.tool
```

**3. Create a new room**
```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"room-404","name":"Server Room","capacity":5}' | python -m json.tool
```

**4. Get a single room by ID**
```bash
curl -s http://localhost:8080/api/v1/rooms/room-101 | python -m json.tool
```

**5. Create a sensor linked to a room**
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"sensor-new-01","type":"TEMPERATURE","status":"ACTIVE","currentValue":20.0,"roomId":"room-101"}' \
  | python -m json.tool
```

**6. List sensors filtered by type**
```bash
curl -s "http://localhost:8080/api/v1/sensors?type=co2" | python -m json.tool
```

**7. Post a reading to a sensor**
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/sensor-temp-01/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.3}' | python -m json.tool
```

**8. List all readings for a sensor**
```bash
curl -s http://localhost:8080/api/v1/sensors/sensor-temp-01/readings | python -m json.tool
```

**9. Attempt to delete a room that has sensors — expect 409 Conflict**
```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/room-101 | python -m json.tool
```

**10. Attempt to post a reading to a MAINTENANCE sensor — expect 403 Forbidden**
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/sensor-temp-02/readings \
  -H "Content-Type: application/json" \
  -d '{"value":0.0}' | python -m json.tool
```

---

## Seed Data

The following data is pre-loaded at startup by `SeedData.java`:

**Rooms**

| ID | Name | Capacity |
|---|---|---|
| `room-101` | Lecture Hall A | 120 |
| `room-202` | Computer Lab B | 40 |
| `room-303` | Conference Room C | 20 |

**Sensors**

| ID | Type | Status | Room |
|---|---|---|---|
| `sensor-temp-01` | TEMPERATURE | ACTIVE | `room-101` |
| `sensor-co2-01` | CO2 | ACTIVE | `room-101` |
| `sensor-hum-01` | HUMIDITY | ACTIVE | `room-202` |
| `sensor-temp-02` | TEMPERATURE | MAINTENANCE | `room-303` |

---

## Error Handling

All error responses share a single unified JSON envelope:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Sensor with ID 'xyz' was not found.",
  "timestamp": "2026-04-24T10:15:30.123456789Z"
}
```

| Scenario | HTTP Status |
|---|---|
| Missing or invalid request field | 400 Bad Request |
| Referenced room does not exist when creating a sensor | 422 Unprocessable Entity |
| Resource with that ID already exists | 409 Conflict |
| Room still has sensors assigned (DELETE blocked) | 409 Conflict |
| Sensor is in MAINTENANCE — cannot accept readings | 403 Forbidden |
| Resource not found | 404 Not Found |
| Unexpected server error | 500 Internal Server Error |

---

## Logging

Every HTTP request and response is logged by `ApiLoggingFilter` via `java.util.logging`:

```
[REQUEST]  POST http://localhost:8080/api/v1/sensors
[RESPONSE] 201 Created
```

---

## Report — Answers to Coursework Questions

---

### Q1 (Part 1.1) — JAX-RS Resource Class Lifecycle

The default lifecycle of a JAX-RS resource class is **per-request**. The runtime creates a brand-new instance of the resource class for every incoming HTTP request and discards it once the response is sent. This means instance fields are never shared between requests and cannot hold persistent data.

This architectural decision has a direct impact on how shared state must be managed. Because each request gets its own object, storing application data in a regular instance field (e.g., `private List<Room> rooms = new ArrayList<>()`) would cause that data to be lost the moment the request completes. To prevent data loss, all mutable state must be externalised into a separate singleton — in this project, `InMemoryStore`. Furthermore, because multiple requests can arrive simultaneously, multiple resource instances will read and write to that shared store concurrently. To prevent race conditions and data corruption, thread-safe data structures are required: this project uses `ConcurrentHashMap` for room and sensor storage and `CopyOnWriteArrayList` for sensor readings. JAX-RS also supports the `@Singleton` annotation to force a single shared resource instance, but this makes the developer responsible for synchronising every method that touches shared state.

---

### Q2 (Part 1.2) — HATEOAS and Hypermedia

HATEOAS (Hypermedia As The Engine Of Application State) is the highest level of REST maturity defined by the Richardson Maturity Model. It is considered a hallmark of advanced RESTful design because it makes the API self-describing: instead of clients relying on external documentation to know which URLs exist and what actions are available, the server embeds navigational links directly inside the JSON response body.

Without HATEOAS, client developers must hardcode every endpoint URL into their application. If the server changes a route, every client that depends on that hardcoded string breaks. With HATEOAS, the client discovers available resources by following links returned by the server — the same way a browser follows hyperlinks on a webpage. In this project the discovery endpoint at `GET /api/v1/` returns a `resources` map containing the URIs for `/api/v1/rooms` and `/api/v1/sensors`, so a client only needs to know the single entry-point URL to navigate the entire API.

---

### Q3 (Part 2.1) — Returning IDs vs Full Room Objects


This is a classic trade-off between network bandwidth and client-side processing.

**Returning full objects** delivers all room data in a single HTTP response. The client can render a dashboard immediately without any follow-up requests. The downside is a larger payload, especially when there are many rooms.

**Returning only IDs** keeps the initial response minimal, saving bandwidth. However, it creates the N+1 problem: a client that needs to display room names and capacities must fire a separate `GET /rooms/{id}` request for every ID in the list. In mobile or high-latency environments, establishing dozens of additional HTTP connections is significantly less efficient than receiving one larger payload upfront.

In this project `GET /api/v1/rooms` returns full room objects, prioritising client convenience and eliminating unnecessary round-trips for typical dashboard use cases.

---

### Q4 (Part 2.2) — DELETE Idempotency

Yes, the `DELETE` operation in this implementation is idempotent.

Idempotency means that making the same request multiple times produces the same server-side state as making it once. In this project, if a client sends `DELETE /api/v1/rooms/room-101` three times:

1. **First request** — the room exists, it is removed from the store, and `200 OK` is returned. The server state has changed: the room is gone.
2. **Second and third requests** — the room is not found in the store, so `404 Not Found` is returned. Crucially, the server state does not change; the room is still absent.

The final state of the server (room does not exist) is identical whether the request was sent once or a hundred times. Different HTTP status codes on repeated calls (200 then 404) are permitted by RFC 7231 — idempotency is a guarantee about server state, not about response codes.

---

### Q5 (Part 3.1) — @Consumes Mismatch Behaviour

The `@Consumes(MediaType.APPLICATION_JSON)` annotation instructs the JAX-RS runtime to route a request to this method only when the client's `Content-Type` header is `application/json`. If a client sends `Content-Type: text/plain` or `Content-Type: application/xml`, Jersey intercepts the request before the method body is ever executed.

The runtime detects the mismatch and automatically returns `415 Unsupported Media Type` to the client. No manual Content-Type checking is needed inside the resource method. The consequence is entirely beneficial: the resource method is protected from receiving data in formats it cannot parse, runtime deserialization errors are eliminated, and the API contract is enforced declaratively through annotations alone.

---

### Q6 (Part 3.2) — @QueryParam vs @PathParam for Filtering

URI path segments identify resources; query parameters modify a query against a collection.

1. **Semantic correctness** — `/api/v1/sensors` identifies the sensors collection. Filtering by type does not create a new, distinct resource; it returns a narrowed view of the same collection. `?type=CO2` correctly expresses this modifier relationship, whereas `/sensors/type/CO2` incorrectly implies that `CO2` is a resource identity on the same level as a sensor ID.

2. **Optionality** — Query parameters are inherently optional. The same endpoint returns all sensors when `?type=` is omitted and a filtered list when it is provided. A path-based design would require two separate routes or an always-mandatory segment.

3. **Composability** — Multiple query parameters stack cleanly: `?type=CO2&status=ACTIVE`. Encoding the same filters as path segments produces deeply nested, rigid URLs that are difficult to route and maintain as the number of filter options grows.

---

### Q7 (Part 4.1) — Sub-Resource Locator Pattern Benefits


The Sub-Resource Locator pattern enforces the Single Responsibility Principle at the routing level. A method annotated with `@Path` but no HTTP method annotation acts as a delegation step: it resolves the path prefix, performs any cross-cutting pre-condition checks, and hands control to a dedicated sub-resource class.

Without this pattern, `SensorResource` would need to contain every reading endpoint in addition to all sensor CRUD logic. As the API grows, this class becomes a "God Object" mixing unrelated concerns — sensor metadata management and historical telemetry processing — in thousands of lines of code that are difficult to read, test, or extend.

In this project the locator method in `SensorResource` validates that the `sensorId` exists, then instantiates and returns a `SensorReadingResource`. That sub-resource class exclusively handles reading operations, knows nothing about the broader sensor collection, and can be unit-tested in complete isolation. The `sensorId` validation runs once at the locator level and protects both `GET` and `POST` readings automatically, eliminating duplicated guard logic.

---

### Q8 (Part 5.2) — Why 422 is More Accurate Than 404

`404 Not Found` signals that the **URL itself does not exist**. If a client sends `POST /api/v1/sensors` and receives a 404, the natural interpretation is that the endpoint does not exist — the client would check for a typo in the path or assume the service is down. That interpretation is entirely wrong: the endpoint exists and is active.

`422 Unprocessable Entity` signals that the URL is valid, the JSON is syntactically correct, but the server cannot process the request due to a **semantic error in the payload** — in this case, a `roomId` field that references a room which has not been registered. The 422 response precisely tells the client: "Your request reached the right place and we understood it, but a value inside your body is logically invalid." This separates payload-logic errors from routing errors and directs the client to fix its data rather than its URL.

---

### Q9 (Part 5.4) — Cybersecurity Risks of Exposing Stack Traces

Exposing Java stack traces in HTTP responses is classified as an **Information Disclosure** vulnerability (OWASP Top 10 A05 — Security Misconfiguration). It hands attackers free reconnaissance data that would otherwise require significant effort to obtain:

1. **Framework and library versions** — a trace typically includes class names from Jersey, Jackson, Jetty, and other dependencies with version numbers. Attackers cross-reference these against CVE databases to find pre-existing exploits for that exact version.

2. **Internal package structure and file paths** — the fully-qualified class names and server-side file paths in a trace reveal the application's directory layout, package naming conventions, and deployment configuration, mapping the internal architecture for an attacker.

3. **Database or query information** — if a data-access error propagates, the trace may expose table names, column names, or a malformed query string, providing a direct blueprint for SQL injection or targeted data extraction attempts.

In this project `GlobalExceptionMapper` solves this by logging the full exception (including stack trace) to the server-side console for operators, while returning only a generic `500 Internal Server Error` message body to the client — acknowledgement of failure with zero internal detail.

---

### Q10 (Part 5.5) — Why Filters Are Better Than Manual Logging

Placing `Logger.info()` calls inside individual resource methods causes several compounding problems:

1. **Violates the DRY principle** — every new endpoint requires the same two log statements to be written again. If the log format ever needs to change, every file in the project must be edited.

2. **Silent gaps on exceptions** — if a resource method throws before the log line is reached, that request is never recorded. Filter-based logging intercepts traffic before routing occurs, so even requests that fail with `404` before touching a resource method are captured.

3. **Wrong layer of responsibility** — a resource method should be responsible for one thing: executing business logic. Embedding observability code inside it mixes concerns and reduces readability.

`ApiLoggingFilter` (implementing `ContainerRequestFilter` and `ContainerResponseFilter`) addresses all three issues. It is registered once and runs automatically for every request and response in the application, guaranteeing complete observability with zero repetition across resource classes.
