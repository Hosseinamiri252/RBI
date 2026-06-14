package com.iwap.exception;

public class SessionCreationException extends RuntimeException {
    public SessionCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
