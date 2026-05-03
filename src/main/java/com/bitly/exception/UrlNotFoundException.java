package com.bitly.exception;

/**
 * Exception thrown when a URL mapping with the requested short code is not found.
 */
public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException(String shortCode) {
        super("URL with short code '" + shortCode + "' not found");
    }
}
