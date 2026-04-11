package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.store.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.Map;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final InMemoryStore store = InMemoryStore.getInstance();

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Validates all required Sensor fields.
     * Returns an error message, or null if validation passes.
     */
    private String validateSensor(Sensor sensor) {
        if (sensor == null) {
            return "Request body is required.";
        }
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return "Field 'id' is required and must not be blank.";
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            return "Field 'type' is required and must not be blank.";
        }
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            return "Field 'status' is required and must not be blank.";
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return "Field 'roomId' is required and must not be blank.";
        }
        return null;
    }

    /**
     * Builds a consistent JSON error response.
     */
    private Response buildError(Response.Status status, String error, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        body.put("message", message);
        return Response.status(status).entity(body).build();
    }
}
