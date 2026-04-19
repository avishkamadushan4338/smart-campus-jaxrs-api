package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final InMemoryStore store = InMemoryStore.getInstance();

    // -------------------------------------------------------------------------
    // POST /api/v1/sensors  —  Create a new sensor
    // -------------------------------------------------------------------------
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        String validationError = validateSensor(sensor);
        if (validationError != null) {
            return buildError(Response.Status.BAD_REQUEST, "Bad Request", validationError);
        }

        if (!store.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        if (store.sensorExists(sensor.getId())) {
            return buildError(Response.Status.CONFLICT, "Conflict",
                    "A sensor with ID '" + sensor.getId() + "' already exists.");
        }

        store.addSensor(sensor);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(sensor.getId())
                .build();

        return Response.created(location).entity(sensor).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors          —  List all sensors
    // GET /api/v1/sensors?type=CO2 —  Filter sensors by type (case-insensitive)
    // -------------------------------------------------------------------------
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = store.getAllSensors();

        if (type == null || type.trim().isEmpty()) {
            return Response.ok(all).build();
        }

        List<Sensor> filtered = all.stream()
                .filter(s -> s.getType().equalsIgnoreCase(type.trim()))
                .collect(Collectors.toList());

        return Response.ok(filtered).build();
    }

    // -------------------------------------------------------------------------
    // Sub-resource locator: delegates /sensors/{sensorId}/readings to
    // SensorReadingResource.  No HTTP method annotation = locator, not handler.
    // -------------------------------------------------------------------------
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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

    private Response buildError(Response.Status status, String error, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", error);
        body.put("message", message);
        return Response.status(status).entity(body).build();
    }
}
