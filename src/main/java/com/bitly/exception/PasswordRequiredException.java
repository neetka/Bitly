package com.bitly.exception;

/**
 * Exception thrown when accessing a password-protected short URL without providing a password.
 */
public class PasswordRequiredException extends RuntimeException {

    public PasswordRequiredException(String shortCode) {
        super("Password is required to access the URL with short code '" + shortCode + "'");
    }
}
