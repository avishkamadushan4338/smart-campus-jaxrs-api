package com.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standard JSON error envelope returned by all exception mappers.
 *
 * Core fields: status, error, message, timestamp.
 * Optional extra context fields are added via {@link #with(String, Object)}.
 */
public class ApiError {

    private final int    status;
    private final String error;
    private final String message;
    private final String timestamp;

    /** Holds optional extra fields flattened into the top-level JSON object. */
    private final Map<String, Object> extras = new LinkedHashMap<>();

    public ApiError(int status, String error, String message, String timestamp) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.timestamp = timestamp;
    }

    /** Convenience factory — auto-generates the current UTC ISO-8601 timestamp. */
    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, Instant.now().toString());
    }

    /**
     * Fluent builder for optional extra context fields.
     * Returns {@code this} so calls can be chained.
     */
    public ApiError with(String key, Object value) {
        extras.put(key, value);
        return this;
    }

    // -----------------------------------------------------------------------
    // Standard getters (serialised by Jackson in declaration order)
    // -----------------------------------------------------------------------

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
