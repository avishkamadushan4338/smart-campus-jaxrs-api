package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ApiError;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Sub-resource that handles all reading operations scoped to one specific sensor.
 * Reached via the sub-resource locator in {@link SensorResource}:
 *
 *   GET  /api/v1/sensors/{sensorId}/readings  — list all readings for this sensor
 *   POST /api/v1/sensors/{sensorId}/readings  — record a new reading
 *
 * This class has no class-level {@code @Path} annotation — the full path is
 * resolved by the locator method in SensorResource, which also performs the
 * 404 guard before this class is ever instantiated.
 */
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String        sensorId;
    private final InMemoryStore store = InMemoryStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}/readings
    // Returns the full reading history for this sensor (empty array if none yet).
    // -------------------------------------------------------------------------
    @GET
    public Response getReadings() {
        List<SensorReading> readings = store.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/sensors/{sensorId}/readings
    // Validates, defaults missing fields, persists, and updates currentValue.
    // -------------------------------------------------------------------------
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {

        String validationError = validateReading(reading);
        if (validationError != null) {
            return buildError(Response.Status.BAD_REQUEST, "Bad Request", validationError);
        }

        // Reject readings for sensors that are under maintenance (403 Forbidden).
        Sensor parentSensor = store.getSensorById(sensorId);
        if (parentSensor != null
                && "MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        // Auto-generate a unique ID if the client did not supply one.
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // Default timestamp to current UTC instant if absent or blank.
        // Stored as ISO-8601 string (e.g. "2026-04-23T10:15:30.123456789Z").
        if (reading.getTimestamp() == null || reading.getTimestamp().trim().isEmpty()) {
            reading.setTimestamp(Instant.now().toString());
        }

        store.addReading(sensorId, reading);

        // Keep the parent sensor's currentValue in sync with the latest reading.
        // This ensures GET /sensors/{id} always reflects the most recent measurement.
        if (parentSensor != null) {
            parentSensor.setCurrentValue(reading.getValue());
        }

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(reading.getId())
                .build();

        return Response.created(location).entity(reading).build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String validateReading(SensorReading reading) {
        if (reading == null) {
            return "Request body is required.";
        }
        // 'value' is a primitive double — Jackson defaults it to 0.0 if absent,
        // which is a legitimate sensor reading, so no extra null check is needed.
        return null;
    }

    private Response buildError(Response.Status status, String error, String message) {
        ApiError body = ApiError.of(status.getStatusCode(), error, message);
        return Response.status(status)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
