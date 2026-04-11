package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton, thread-safe in-memory store for all domain objects.
 * ConcurrentHashMap is used so concurrent requests don't corrupt state.
 */
public class InMemoryStore {

    private static final InMemoryStore INSTANCE = new InMemoryStore();

    private final ConcurrentHashMap<String, Room>   rooms   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();

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
}
