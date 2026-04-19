package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sub-resource that handles all reading operations scoped to one specific sensor.
 * Reached via the sub-resource locator in SensorResource:
 *   GET  /api/v1/sensors/{sensorId}/readings
 *   POST /api/v1/sensors/{sensorId}/readings
 *
 * No @Path at the class level — the path is fully resolved by the locator.
 */
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final InMemoryStore store = InMemoryStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }
}
