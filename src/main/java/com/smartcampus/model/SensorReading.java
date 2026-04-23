package com.smartcampus.model;

/**
 * Represents a single timestamped reading recorded by a sensor.
 *
 * The {@code timestamp} field is stored as an ISO-8601 UTC string
 * (e.g. "2026-04-23T10:15:30.123Z") rather than a raw epoch number.
 * This makes the JSON response self-documenting and human-readable
 * without any client-side conversion.
 *
 * Jackson requires a no-arg constructor for JSON deserialization.
 */
public class SensorReading {

    private String id;
    private String timestamp;   // ISO-8601 UTC, e.g. "2026-04-23T10:15:30.123Z"
    private double value;

    public SensorReading() {}

    public SensorReading(String id, String timestamp, double value) {
        this.id        = id;
        this.timestamp = timestamp;
        this.value     = value;
    }

    public String getId()        { return id; }
    public String getTimestamp() { return timestamp; }
    public double getValue()     { return value; }

    public void setId(String id)                { this.id = id; }
    public void setTimestamp(String timestamp)  { this.timestamp = timestamp; }
    public void setValue(double value)          { this.value = value; }
}
