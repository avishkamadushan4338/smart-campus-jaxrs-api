package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ApiError;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Sub-resource that handles all reading operations scoped to one specific sensor.
 * Reached via the sub-resource locator in SensorResource:
 *   GET  /api/v1/sensors/{sensorId}/readings
 *   POST /api/v1/sensors/{sensorId}/readings
 *
 * No @Path at the class level — the path is fully resolved by the locator.
 */
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
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
    // Appends a new reading and updates the parent sensor's currentValue.
    // -------------------------------------------------------------------------
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        String validationError = validateReading(reading);
        if (validationError != null) {
            return buildError(Response.Status.BAD_REQUEST, "Bad Request", validationError);
        }

        // Reject readings for sensors that are under maintenance.
        Sensor parentSensor = store.getSensorById(sensorId);
        if (parentSensor != null
                && "MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        // Auto-generate id if missing
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // Use current system time if timestamp is absent or invalid
        if (reading.getTimestamp() <= 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.addReading(sensorId, reading);

        // Keep the parent sensor's currentValue in sync with the latest reading
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
        // 'value' is a primitive double — Jackson deserializes it to 0.0 if
        // the field is absent, which is a valid sensor value, so no extra check.
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
