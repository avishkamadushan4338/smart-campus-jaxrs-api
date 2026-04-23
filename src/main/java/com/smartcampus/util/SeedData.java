package com.smartcampus.util;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.InMemoryStore;

import java.util.logging.Logger;

/**
 * Optional sample data loader called once at application startup.
 * Pre-populates the in-memory store with a small set of rooms and sensors
 * so the API is immediately usable for demo and viva without manual setup.
 *
 * Idempotent — skips any entry that already exists, so redeployments or
 * hot-reloads in development do not create duplicates.
 */
public final class SeedData {

    private static final Logger LOGGER = Logger.getLogger(SeedData.class.getName());

    private SeedData() {}

    public static void load() {
        InMemoryStore store = InMemoryStore.getInstance();

        // --- Seed Rooms ---
        seedRoom(store, new Room("room-101", "Lecture Hall A", 120));
        seedRoom(store, new Room("room-202", "Computer Lab B", 40));
        seedRoom(store, new Room("room-303", "Conference Room C", 20));

        // --- Seed Sensors (roomId must match an existing room) ---
        seedSensor(store, new Sensor("sensor-temp-01", "TEMPERATURE", "ACTIVE", 22.5, "room-101"));
        seedSensor(store, new Sensor("sensor-co2-01",  "CO2",         "ACTIVE", 412.0, "room-101"));
        seedSensor(store, new Sensor("sensor-hum-01",  "HUMIDITY",    "ACTIVE", 55.3, "room-202"));
        seedSensor(store, new Sensor("sensor-temp-02", "TEMPERATURE", "MAINTENANCE", 0.0, "room-303"));

        LOGGER.info("[SeedData] Sample data loaded into InMemoryStore.");
    }

    private static void seedRoom(InMemoryStore store, Room room) {
        if (!store.roomExists(room.getId())) {
            store.addRoom(room);
        }
    }

    private static void seedSensor(InMemoryStore store, Sensor sensor) {
        if (!store.sensorExists(sensor.getId())) {
            store.addSensor(sensor);
            // Keep the parent room's sensorIds list consistent with seeded sensors
            Room room = store.getRoomById(sensor.getRoomId());
            if (room != null && !room.getSensorIds().contains(sensor.getId())) {
                room.getSensorIds().add(sensor.getId());
            }
        }
    }
}
