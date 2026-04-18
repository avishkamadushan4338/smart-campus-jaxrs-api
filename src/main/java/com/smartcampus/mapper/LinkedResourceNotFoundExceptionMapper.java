package com.smartcampus.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps {@link LinkedResourceNotFoundException} to HTTP 422 Unprocessable Entity
 * with a structured JSON body.
 *
 * Scenario: a client submits a syntactically valid JSON payload for a new
 * Sensor, but the referenced {@code roomId} does not exist in the store.
 * HTTP 422 is semantically correct here because the request was well-formed
 * but could not be processed due to a failed domain constraint.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    // 422 Unprocessable Entity — available in JAX-RS 2.1 / Jersey 2.x
    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("linkedResourceType", ex.getResourceType());
        body.put("linkedResourceId", ex.getResourceId());

        return Response.status(Response.Status.NOT_FOUND)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
