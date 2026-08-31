package com.optimizely.scheduler.exception;

/**
 * Thrown when a requested resource does not exist or does not belong
 * to the authenticated user. Mapped to HTTP 404 by the global handler.
 * A 404 (rather than 403) is returned for foreign resources so the
 * existence of another user's data is not revealed.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
