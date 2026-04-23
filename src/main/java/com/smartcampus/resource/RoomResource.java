package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ApiError;
import com.smartcampus.model.Room;
import com.smartcampus.store.InMemoryStore;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final InMemoryStore store = InMemoryStore.getInstance();

    // -------------------------------------------------------------------------
    // GET /api/v1/rooms  —  List all rooms
    // -------------------------------------------------------------------------
    @GET
    public Response getAllRooms() {
        Collection<Room> rooms = store.getAllRooms();
        return Response.ok(rooms).build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/rooms  —  Create a new room
    // -------------------------------------------------------------------------
    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        String validationError = validateRoom(room);
        if (validationError != null) {
            return buildError(Response.Status.BAD_REQUEST, "Bad Request", validationError);
        }

        if (store.roomExists(room.getId())) {
            return buildError(Response.Status.CONFLICT, "Conflict",
                    "A room with ID '" + room.getId() + "' already exists.");
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new CopyOnWriteArrayList<>());
        }

        store.addRoom(room);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();

        return Response.created(location).entity(room).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/rooms/{roomId}  —  Get a single room
    // -------------------------------------------------------------------------
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRoomById(roomId);
        if (room == null) {
            return buildError(Response.Status.NOT_FOUND, "Not Found",
                    "Room with ID '" + roomId + "' was not found.");
        }
        return Response.ok(room).build();
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/rooms/{roomId}  —  Delete a room (only if sensor-free)
    // -------------------------------------------------------------------------
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoomById(roomId);

        if (room == null) {
            return buildError(Response.Status.NOT_FOUND, "Not Found",
                    "Room with ID '" + roomId + "' was not found.");
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }

        store.deleteRoom(roomId);

        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", "Room '" + roomId + "' deleted successfully.");
        return Response.ok(body).build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String validateRoom(Room room) {
        if (room == null) {
            return "Request body is required.";
        }
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return "Field 'id' is required and must not be blank.";
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return "Field 'name' is required and must not be blank.";
        }
        if (room.getCapacity() <= 0) {
            return "Field 'capacity' is required and must be a positive integer.";
        }
        return null;
    }

    private Response buildError(Response.Status status, String error, String message) {
        ApiError body = ApiError.of(status.getStatusCode(), error, message);
        return Response.status(status)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
