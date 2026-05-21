package com.bitly.exception;

/**
 * Exception thrown when a user attempts to access or modify a resource they do not own.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
