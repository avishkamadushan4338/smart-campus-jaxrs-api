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
 * Security contract:
 * - The full exception (class name, message, stack trace) is written to the
 *   server log so operators can diagnose issues.
 * - The HTTP response contains ONLY a generic, user-safe message — no class
 *   names, no stack traces, no internal paths are ever exposed to the caller.
 *
 * Note: {@link WebApplicationException} subclasses (e.g. those thrown
 * explicitly by resource methods with a built-in Response) bypass this mapper
 * because Jersey resolves more specific ExceptionMapper implementations first.
 * This mapper intentionally re-routes those to preserve their original status.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {

        // WebApplicationExceptions already carry an intended HTTP status.
        // Delegate to their built-in response rather than swallowing them.
        if (ex instanceof WebApplicationException) {
            Response original = ((WebApplicationException) ex).getResponse();
            // Only wrap if the body is not already JSON (avoids double-wrapping).
            if (original.getMediaType() != null
                    && original.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                return original;
            }
            // Re-emit with a clean ApiError body.
            int code = original.getStatus();
            String phrase = original.getStatusInfo().getReasonPhrase();
            ApiError body = ApiError.of(code, phrase, ex.getMessage());
            return Response.status(code)
                    .entity(body)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Log the full details server-side for diagnosis — never sent to the client.
        LOGGER.log(Level.SEVERE,
                "Unhandled exception intercepted by GlobalExceptionMapper", ex);

        ApiError body = ApiError.of(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Internal Server Error",
                "An unexpected error occurred. Please contact support "
                        + "if the problem persists."
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
