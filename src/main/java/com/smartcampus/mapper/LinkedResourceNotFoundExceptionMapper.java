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
 * Maps LinkedResourceNotFoundException to HTTP 404 Not Found with a JSON body.
 * Jersey discovers this automatically via @Provider and package scanning.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

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
