package com.smartcampus.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {

    private String id;
    private String name;
    private int    capacity;
    private List<String> sensorIds;

    public Room() {
        this.sensorIds = new CopyOnWriteArrayList<>();
    }

    public Room(String id, String name, int capacity) {
        this.id       = id;
        this.name     = name;
        this.capacity = capacity;
        this.sensorIds = new CopyOnWriteArrayList<>();
    }

    // --- Getters ---

    public String getId()       { return id; }
    public String getName()     { return name; }
    public int    getCapacity() { return capacity; }

    public List<String> getSensorIds() { return sensorIds; }

    // --- Setters ---

    public void setId(String id)           { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setCapacity(int capacity)  { this.capacity = capacity; }

    /**
     * Jackson calls this setter when deserializing JSON that includes a
     * "sensorIds" array.  We copy the incoming list into a CopyOnWriteArrayList
     * so that concurrent POST /sensors requests never corrupt the list while
     * it is being iterated (e.g., during GET /rooms or DELETE /rooms checks).
     */
    public void setSensorIds(List<String> sensorIds) {
        if (sensorIds == null) {
            this.sensorIds = new CopyOnWriteArrayList<>();
        } else {
            this.sensorIds = new CopyOnWriteArrayList<>(sensorIds);
        }
    }
}
