package com.smartcampus.exception;

/**
 * Thrown when a DELETE is attempted on a room that still has sensors assigned.
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {

    private final String roomId;

    public RoomNotEmptyException(String roomId) {
        super("Room '" + roomId + "' cannot be deleted because it still has active sensors assigned.");
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
