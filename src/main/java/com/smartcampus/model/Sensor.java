package com.smartcampus.model;

/**
 * Represents a sensor deployed in a campus room.
 * Jackson requires a no-arg constructor for JSON deserialization.
 */
public class Sensor {

    private String id;
    private String type;
    private String status;
    private double currentValue;
    private String roomId;

    public Sensor() {}

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    public String getId()           { return id; }
    public String getType()         { return type; }
    public String getStatus()       { return status; }
    public double getCurrentValue() { return currentValue; }
    public String getRoomId()       { return roomId; }

    public void setId(String id)                     { this.id = id; }
    public void setType(String type)                 { this.type = type; }
    public void setStatus(String status)             { this.status = status; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
    public void setRoomId(String roomId)             { this.roomId = roomId; }
}
