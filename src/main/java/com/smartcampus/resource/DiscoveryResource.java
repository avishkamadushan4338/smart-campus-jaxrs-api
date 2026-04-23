package com.smartcampus.resource;

import com.smartcampus.model.DiscoveryResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serves GET /api/v1/ — an entry-point discovery document listing available
 * resource collections and basic API metadata.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiInfo() {
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms",   "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");

        DiscoveryResponse body = new DiscoveryResponse(
                "Smart Campus API",
                "v1",
                "RESTful API for managing campus rooms, sensors, and sensor readings.",
                "admin@smartcampus.com",
                resources
        );

        return Response.ok(body).build();
    }
}
