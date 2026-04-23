# Smart Campus JAX-RS API

REST API for the **5COSC022W Client-Server Architectures** coursework.  
Manages campus rooms, IoT sensors, and sensor readings using a pure JAX-RS / Jersey stack with no database.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 8+ |
| REST framework | JAX-RS 2.1 (Jersey 2.41) |
| JSON serialisation | Jackson (via `jersey-media-json-jackson`) |
| Servlet container | Embedded Jetty 9 (via `jetty-maven-plugin`) |
| Build tool | Apache Maven |
| Storage | Thread-safe in-memory (`ConcurrentHashMap`) |

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

The path prefix `/api/v1` is configured once in `src/main/webapp/WEB-INF/web.xml`.

---

## Endpoint Summary

### Discovery

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/` | API discovery — returns name, version, and resource links |

### Rooms

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/rooms` | 200 | List all rooms |
| POST | `/api/v1/rooms` | 201 | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | 200 / 404 | Get a room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | 200 / 404 / 409 | Delete a room (blocked if sensors are assigned) |

### Sensors

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/sensors` | 200 | List all sensors (optional `?type=` filter) |
| POST | `/api/v1/sensors` | 201 | Create a new sensor linked to a room |
| GET | `/api/v1/sensors/{sensorId}` | 200 / 404 | Get a sensor by ID |

### Sensor Readings

| Method | Path | Status | Description |
|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | 200 / 404 | List all readings for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | 201 / 403 / 404 | Record a new reading (blocked if sensor is MAINTENANCE) |

---

## Sample curl Commands

> The server ships with seed data (rooms `room-101`, `room-202`, `room-303` and four sensors) so these commands work immediately after `mvn jetty:run`.

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

**4. Create a sensor linked to a room**
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"sensor-new-01","type":"TEMPERATURE","status":"ACTIVE","currentValue":20.0,"roomId":"room-101"}' | python -m json.tool
```

**5. Get a single sensor by ID**
```bash
curl -s http://localhost:8080/api/v1/sensors/sensor-temp-01 | python -m json.tool
```

**6. Post a reading to a sensor**
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/sensor-temp-01/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}' | python -m json.tool
```

**7. Attempt to delete a room that has sensors (expect 409 Conflict)**
```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/room-101 | python -m json.tool
```

**8. Filter sensors by type**
```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2" | python -m json.tool
```

---

## Error Handling

All error responses share a single unified JSON envelope:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Sensor with ID 'xyz' was not found.",
  "timestamp": "2026-04-23T10:15:30.123Z"
}
```

Some errors include additional context fields (e.g. `roomId`, `sensorId`, `linkedResourceType`).

| Scenario | HTTP Status |
|---|---|
| Invalid / missing request field | 400 Bad Request |
| Resource with that ID already exists | 409 Conflict |
| Referenced room does not exist when creating a sensor | 422 Unprocessable Entity |
| Sensor is in MAINTENANCE, cannot accept readings | 403 Forbidden |
| Resource not found | 404 Not Found |
| Unexpected server error | 500 Internal Server Error |

Exception mappers (`com.smartcampus.mapper`) handle domain exceptions globally so resource classes stay clean.  
A catch-all `GlobalExceptionMapper` ensures no raw stack traces are ever sent to the client.

---

## Logging

Every HTTP request and response is logged by `ApiLoggingFilter` (a JAX-RS `ContainerRequestFilter` / `ContainerResponseFilter`):

```
[REQUEST]  GET http://localhost:8080/api/v1/rooms
[RESPONSE] 200 OK
```

Logs are written via `java.util.logging` and appear in the Jetty console during development.

---

## Project Structure

```
src/main/java/com/smartcampus/
├── config/    SmartCampusApplication.java   — Jersey ResourceConfig entry point
├── model/     Room, Sensor, SensorReading, ApiError, DiscoveryResponse
├── resource/  RoomResource, SensorResource, SensorReadingResource, DiscoveryResource
├── store/     InMemoryStore (singleton ConcurrentHashMap)
├── exception/ LinkedResourceNotFoundException, RoomNotEmptyException, SensorUnavailableException
├── mapper/    Exception → HTTP response mappers + GlobalExceptionMapper
├── filter/    ApiLoggingFilter
└── util/      SeedData (optional sample data loaded at startup)
```
