package com.smartcampus.mapper;

import com.smartcampus.model.ApiError;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Catch-all safety net for every {@link Throwable} that is not handled by a
 * more specific mapper.
 *
 * Security contract: the full exception is written to the server log but the
 * HTTP response contains ONLY a generic user-safe message.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
