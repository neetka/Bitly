package com.bitly.exception;

/**
 * Exception thrown when attempting to access an expired URL mapping.
 */
public class UrlExpiredException extends RuntimeException {

    public UrlExpiredException(String shortCode) {
        super("URL with short code '" + shortCode + "' has expired");
    }
}
