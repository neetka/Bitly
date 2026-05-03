package com.bitly.exception;

/**
 * Exception thrown when a requested custom alias is already taken.
 */
public class ShortCodeAlreadyExistsException extends RuntimeException {

    public ShortCodeAlreadyExistsException(String alias) {
        super("Custom alias '" + alias + "' is already in use");
    }
}
