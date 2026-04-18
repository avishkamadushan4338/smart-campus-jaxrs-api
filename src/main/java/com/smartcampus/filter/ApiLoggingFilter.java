package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Single filter class that handles both request and response logging for every
 * API call, acting as a cross-cutting concern without touching resource code.
 *
 * Logged per request  : HTTP method + full request URI
 * Logged per response : final HTTP status code
 *
 * Jersey discovers this class automatically via {@code @Provider} and the
 * package scan configured in {@code SmartCampusApplication}.
 */
@Provider
public class ApiLoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER =
            Logger.getLogger(ApiLoggingFilter.class.getName());

    /**
     * Invoked by Jersey before the request reaches any resource method.
     * Logs the HTTP method and the full request URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format("[REQUEST]  %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }
}
