package com.example.authentication.config;

import com.example.authentication.exception.AppInternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({
            AppInternalException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<String> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> uncaughtException(Exception e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}
