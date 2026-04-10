package com.smartcampus.exception;

/**
 * Thrown when a resource references a linked resource that does not exist.
 * For example, creating a sensor with a roomId that has not been registered.
 * Mapped to HTTP 404 Not Found by LinkedResourceNotFoundExceptionMapper.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public LinkedResourceNotFoundException(String resourceType, String resourceId) {
        super("Linked " + resourceType + " with ID '" + resourceId + "' was not found.");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
