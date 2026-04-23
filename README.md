# Smart Campus JAX-RS API

REST API for the **5COSC022W Client-Server Architectures** coursework.  
Manages campus rooms, IoT sensors, and sensor readings using a pure JAX-RS / Jersey stack with no database.

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
│   ├── Room.java                     — Room domain object
│   ├── Sensor.java                   — Sensor domain object
│   ├── SensorReading.java            — Reading domain object (ISO-8601 timestamp)
│   ├── ApiError.java                 — Unified JSON error envelope
│   └── DiscoveryResponse.java        — Discovery endpoint response body
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
│   └── ApiLoggingFilter.java         — Request/response logging (ContainerFilter)
└── util/
    └── SeedData.java                 — Demo data loaded at startup
```

---

## Build and Run

### Prerequisites
- JDK 8 or later
- Maven 3.6 or later

### Start the server

```bash
mvn jetty:run
```

The server starts on **http://localhost:8080**.  
Seed data (3 rooms, 4 sensors) is loaded automatically so all endpoints are immediately testable.

### Build a deployable WAR

```bash
mvn clean package
```

The WAR is written to `target/smart-campus-jaxrs-api-1.0-SNAPSHOT.war`.

---

## Base URL

```
http://localhost:8080/api/v1
```

The path prefix `/api/v1` is configured once in `src/main/webapp/WEB-INF/web.xml` via `<url-pattern>/api/v1/*</url-pattern>`.

---

## Endpoint Summary

### Discovery

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/` | 200 | API discovery — returns name, version, admin contact, and resource links |

### Rooms

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/rooms` | 200 | List all rooms |
| POST | `/api/v1/rooms` | 201 / 400 / 409 | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | 200 / 404 | Get a room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | 200 / 404 / 409 | Delete a room (blocked if sensors are assigned) |

### Sensors

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/sensors` | 200 | List all sensors (optional `?type=` filter, case-insensitive) |
| POST | `/api/v1/sensors` | 201 / 400 / 409 / 422 | Create a sensor linked to an existing room |
| GET | `/api/v1/sensors/{sensorId}` | 200 / 404 | Get a sensor by ID |

### Sensor Readings (Sub-Resource)

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | 200 / 404 | List all readings for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | 201 / 400 / 403 / 404 | Record a new reading |

---

## Sample curl Commands

> The server ships with seed data so all commands work immediately after `mvn jetty:run`.

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

**6. List sensors filtered by type (case-insensitive)**
```bash
curl -s "http://localhost:8080/api/v1/sensors?type=co2" | python -m json.tool
```

**7. Post a reading to a sensor (timestamp auto-generated if omitted)**
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

## Error Handling

All error responses share a single unified JSON envelope:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Sensor with ID 'xyz' was not found.",
  "timestamp": "2026-04-23T10:15:30.123456789Z"
}
```

Some errors include additional context fields (e.g. `roomId`, `sensorId`, `linkedResourceType`).

| Scenario | HTTP Status |
|---|---|
| Missing or invalid request field | 400 Bad Request |
| Referenced room does not exist when creating a sensor | 422 Unprocessable Entity |
| Resource with that ID already exists | 409 Conflict |
| Room still has sensors assigned (DELETE blocked) | 409 Conflict |
| Sensor is in MAINTENANCE — cannot accept readings | 403 Forbidden |
| Resource not found | 404 Not Found |
| Unexpected server error | 500 Internal Server Error |

Exception mappers in `com.smartcampus.mapper` handle domain exceptions globally.  
`GlobalExceptionMapper` ensures no raw stack traces or Jetty HTML error pages are ever sent to the client.

---

## Logging

Every HTTP request and response is logged by `ApiLoggingFilter`:

```
[REQUEST]  POST http://localhost:8080/api/v1/sensors
[RESPONSE] 201 Created
```

Logs are written via `java.util.logging` to the Jetty console.

---

## Seed Data (for Demo / Viva)

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

## Theory / Report Answers

The following answers address all theory sub-questions embedded in each part of the coursework specification.

---

### Part 1 — T1: JAX-RS Resource Lifecycle

By default JAX-RS creates a **new instance of each resource class for every HTTP request** (per-request scope). This is the safest default because each request gets its own clean object — there is no shared mutable field state between threads. Jersey also supports `@Singleton` scope, where one instance handles all requests simultaneously; this requires every field to be thread-safe.

In this project all resource classes use the default per-request scope. Shared state lives exclusively in `InMemoryStore`, a singleton whose maps and lists use `ConcurrentHashMap` and `CopyOnWriteArrayList`. This combination is correct: stateless request handlers operating on a thread-safe shared store.

---

### Part 1 — T2: HATEOAS / Hypermedia

**HATEOAS** (Hypermedia As The Engine Of Application State) is a REST constraint where API responses embed links to related resources and next available actions. A client navigating a fully HATEOAS API needs to know only the entry-point URL — every subsequent URL is discovered from links in responses.

In this project two HATEOAS principles are applied:

1. `GET /api/v1/` (the discovery endpoint) returns a `resources` map with the URIs of every top-level collection. A client can discover `/api/v1/rooms` and `/api/v1/sensors` without any prior knowledge of the API structure.
2. Every `201 Created` response includes a `Location` header pointing to the newly created resource URI. The client immediately knows where to retrieve the resource without constructing the URL itself.

Full HATEOAS would additionally embed `_links` objects in every response body (e.g. a sensor response would link to its parent room and to its readings collection).

---

### Part 2 — T3: Returning IDs vs Full Objects in `sensorIds`

`Room` stores `List<String> sensorIds` rather than `List<Sensor>` for three reasons:

1. **Avoids circular reference**: `Sensor` has a `roomId` field that points back to the room. Embedding full `Sensor` objects inside `Room` would create an object graph cycle, causing infinite recursion during JSON serialisation.
2. **Reduces payload size**: A room with many sensors would embed all sensor data on every `GET /rooms` call, even when the client only needs room metadata. Storing IDs lets the client fetch sensor detail lazily via `GET /sensors/{id}`.
3. **Follows REST resource identity conventions**: In REST, each resource is independently addressable. A room representation links to its sensors by their identifiers; clients retrieve the full sensor representation from the sensor's own endpoint.

---

### Part 2 — T4: DELETE Idempotency

HTTP `DELETE` is defined as **idempotent** — applying it multiple times must leave the server in the same final state as a single application.

In this project:
- The **first** `DELETE /rooms/{roomId}` removes the room and returns `200 OK`.
- A **second** `DELETE /rooms/{roomId}` finds nothing to delete and returns `404 Not Found`.

This is correct and does not violate idempotency. Idempotency is a **server-state guarantee**, not a status-code guarantee. The resource is absent after any number of DELETE calls. The different HTTP status codes (200 vs 404) are explicitly permitted by RFC 7231 — what must be consistent is the stored state, not the response.

---

### Part 3 — T5: `@Consumes` Mismatch Behaviour

When a JAX-RS method is annotated `@Consumes(MediaType.APPLICATION_JSON)` and a client sends `Content-Type: text/plain` or `Content-Type: application/xml`, **Jersey automatically returns `415 Unsupported Media Type`** before the resource method is ever reached. No manual Content-Type checking is needed inside the resource class.

In this project, `GlobalExceptionMapper` intercepts the resulting `WebApplicationException`, reads its status, and wraps it in a structured `ApiError` JSON body. This means even this framework-level rejection produces a consistent error envelope — the client always receives `{"status":415,"error":"Unsupported Media Type",...}` and never a raw Jetty HTML page.

---

### Part 3 — T6: `@QueryParam` vs `@PathParam` for Filtering

`@PathParam` identifies a **specific, unique resource** — `/sensors/sensor-001` unambiguously refers to exactly one sensor. Path segments represent resource identity.

`@QueryParam` is the correct tool for **filtering or modifying a collection query**. `GET /sensors?type=CO2` returns a subset of the sensors collection matching a condition. Key advantages:

- The filter is **optional** — omitting `?type=` returns the full collection automatically.
- The resource identity (`/sensors`) does not change; only the result set is narrowed.
- Multiple filters can be composed easily (`?type=CO2&status=ACTIVE`).
- Using a path segment for a filter (`/sensors/type/CO2`) incorrectly implies `CO2` is a resource identity, not a query criterion.

---

### Part 4 — T7: Sub-Resource Locator Pattern Benefits

A **sub-resource locator** is a JAX-RS method annotated with `@Path` but *no* HTTP method annotation (`@GET`, `@POST`, etc.). Jersey treats it as a routing step rather than a request handler: it resolves the path prefix, then delegates further routing to the returned object.

Benefits in this project:

| Benefit | Applied here |
|---|---|
| **Separation of concerns** | `SensorResource` handles sensor CRUD; all reading logic is in `SensorReadingResource` |
| **Pre-condition validation** | The locator verifies the `sensorId` exists before `SensorReadingResource` is instantiated — one guard protects both `GET` and `POST` readings |
| **Incremental path resolution** | Jersey composes `/sensors/{id}` from the locator's `@Path` with `/readings` from sub-resource methods |
| **Smaller, focused classes** | Each class has a single clear purpose that can be explained in one sentence |

---

### Part 4 — T8: Why Nested Resources Belong in Separate Classes

Placing every endpoint in one resource class creates a **God class** that violates the Single Responsibility Principle. Problems avoided by the split:

- `SensorReadingResource` carries its own `sensorId` field injected via the constructor — impossible if everything lived in one class.
- Method names no longer collide (`getReadings()` vs `getSensors()`).
- Each class can be extended or tested independently without risk of breaking the other.
- During a code review or viva, each class can be described in one sentence: "This class handles everything related to readings for a specific sensor."

---

### Part 4 — T9: Why Posting a Reading Must Update `currentValue`

`sensor.currentValue` represents the **latest known measurement**. Without updating it on every `POST /readings`, `GET /sensors/{id}` would permanently return the value set at sensor creation — a stale snapshot, not live data.

Applications using this API (dashboards, alert systems, monitoring tools) rely on `currentValue` for real-time decisions: "Is the CO₂ level currently above the safety threshold?" Requiring them to fetch the full reading history and sort by timestamp just to get the latest value is wasteful. By updating `currentValue` atomically on every successful `POST /readings`, the API provides:

- **Fast access to the latest measurement** via a single field on `GET /sensors/{id}`
- **Full historical data** via `GET /sensors/{id}/readings`

Both are available simultaneously — clients choose the appropriate level of detail.

---

### Part 5 — T10: Why 422 Unprocessable Entity is More Accurate Than 404

`404 Not Found` means **the requested URL does not exist**. Returning 404 from `POST /sensors` would make the client believe the `/sensors` endpoint itself is missing — a routing error.

`422 Unprocessable Entity` means **the request URL was correct and the JSON was syntactically valid, but the payload contained a semantic error** — in this case `roomId` references a room that has not been registered. The JSON parsed successfully; the endpoint exists; the constraint that the referenced room must exist was simply not satisfied.

| Status | Client interprets as | Correct action for client |
|---|---|---|
| 404 | Wrong URL — fix the endpoint address | Change the URL |
| 422 | Wrong data — fix the request payload | Fix the `roomId` value |

Using 422 gives the client exactly the information it needs to correct its request.

---

### Part 5 — T11: Security Risks of Exposing Stack Traces

A stack trace in an HTTP response exposes:

1. **Internal package and class names** — reveals application structure and identifies the technology stack.
2. **Server-side file paths** — directory layout can indicate deployment configuration.
3. **Framework and library names with version numbers** — enables targeted attacks using known CVEs for that exact version (e.g. a known Jersey or Jackson vulnerability).
4. **Method signatures and internal logic** — reveals implementation details that help an attacker craft exploit inputs.

This is classified as **Information Exposure / Security Misconfiguration** (OWASP Top 10 A05). In this project `GlobalExceptionMapper` addresses this with a two-channel approach:

- The **full exception** (class name, message, stack trace) is written to the **server-side log** so operators can diagnose issues.
- The **HTTP response** contains only a generic, user-safe message. No class names, file paths, or version strings are ever sent to the client.

---

### Part 5 — T12: Why Filters Are Better Than Manual Logging in Every Resource Method

Placing `Logger.info(...)` calls inside individual resource methods causes:

| Problem | Consequence |
|---|---|
| **Repetitive boilerplate** | Every new endpoint needs the same two log statements |
| **Error-prone omissions** | One forgotten log call creates a silent gap in audit records |
| **Inconsistent format** | Developers write different log strings; log aggregation tools fail |
| **Wrong responsibility** | Resource methods should handle HTTP logic, not observability |
| **Skipped on exceptions** | If a resource throws before the log line, no entry is written |

`ApiLoggingFilter` (implementing `ContainerRequestFilter` + `ContainerResponseFilter`) solves all of these:

- **Guaranteed execution** — Jersey invokes it for every request, even when exceptions propagate through the stack.
- **Cannot be accidentally omitted** — there is one place for log logic; new endpoints require no log code.
- **Single format** — defined once, consistent everywhere.
- **Cross-cutting concern correctly separated** — logging is orthogonal to domain logic and belongs outside resource classes.

This is the standard application of the **Intercepting Filter** design pattern in JAX-RS / Java EE applications.
