package com.smartcampus.exception;

/**
 * Thrown when a POST reading is attempted on a sensor whose status is
 * "MAINTENANCE".  Mapped to HTTP 403 Forbidden by
 * SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;

    public SensorUnavailableException(String sensorId) {
        super("Sensor '" + sensorId + "' is currently under maintenance "
                + "and cannot accept new readings.");
        this.sensorId = sensorId;
    }
}
