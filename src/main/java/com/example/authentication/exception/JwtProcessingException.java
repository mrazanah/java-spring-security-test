package com.example.authentication.exception;

public class JwtProcessingException extends AppInternalException {
    public JwtProcessingException(String message) {
        super(message);
    }
}
