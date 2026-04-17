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
}
