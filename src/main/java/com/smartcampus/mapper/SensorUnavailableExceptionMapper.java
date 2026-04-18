package com.smartcampus.mapper;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ApiError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps {@link SensorUnavailableException} to HTTP 403 Forbidden with a
 * structured JSON body.
 *
 * Scenario: a client tries to POST a new reading to a sensor whose status
 * is "MAINTENANCE".
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ApiError body = ApiError.of(
                Response.Status.FORBIDDEN.getStatusCode(),
                "Forbidden",
                ex.getMessage()
        ).with("sensorId", ex.getSensorId());

        return Response.status(Response.Status.FORBIDDEN)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
