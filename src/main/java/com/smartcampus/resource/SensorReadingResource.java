package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final InMemoryStore store = InMemoryStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> readings = store.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        String validationError = validateReading(reading);
        if (validationError != null) {
            return buildError(Response.Status.BAD_REQUEST, "Bad Request", validationError);
        }

        Sensor parentSensor = store.getSensorById(sensorId);
        if (parentSensor != null
                && "MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() <= 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.addReading(sensorId, reading);

        // Keep the parent sensor's currentValue in sync with the latest reading
        if (parentSensor != null) {
            parentSensor.setCurrentValue(reading.getValue());
        }

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }

    private String validateReading(SensorReading reading) {
        if (reading == null) {
            return "Request body is required.";
        }
        return null;
    }

    private Response buildError(Response.Status status, String error, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        body.put("message", message);
        return Response.status(status).entity(body).build();
    }
}
