package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Singleton, thread-safe in-memory store for all domain objects.
 * ConcurrentHashMap is used so concurrent requests don't corrupt state.
 */
public class InMemoryStore {

    private static final InMemoryStore INSTANCE = new InMemoryStore();

    private final ConcurrentHashMap<String, Room>              rooms    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Sensor>            sensors  = new ConcurrentHashMap<>();
    // Key = sensorId, Value = thread-safe list of readings for that sensor
    private final ConcurrentHashMap<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private InMemoryStore() {}

    public static InMemoryStore getInstance() {
        return INSTANCE;
    }

    // --- Room operations ---

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public Room getRoomById(String id) {
        return rooms.get(id);
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    // --- Sensor operations ---

    public Collection<Sensor> getAllSensors() {
        return sensors.values();
    }

    public Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    // --- SensorReading operations ---

    /**
     * Returns the full reading history for the given sensor.
     * Returns an empty list if no readings have been recorded yet.
     */
    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    /**
     * Appends a new reading to the history for the given sensor.
     * Uses computeIfAbsent so the list is created atomically on first use.
     */
    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>()).add(reading);
    }
}
