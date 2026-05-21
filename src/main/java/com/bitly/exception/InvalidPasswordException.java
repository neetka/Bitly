package com.bitly.exception;

/**
 * Exception thrown when the wrong password is provided for a password-protected short URL.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("Invalid password provided for this shortened URL");
    }
}
