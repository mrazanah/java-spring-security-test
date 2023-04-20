package com.example.authentication.exception;

public abstract class AppInternalException extends RuntimeException {
    public AppInternalException(String message) {
        super(message);
    }

    public AppInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
