package com.iwap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> notFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(error(e.getMessage(), 404));
    }

    @ExceptionHandler(PolicyViolationException.class)
    public ResponseEntity<?> forbidden(PolicyViolationException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(error(e.getMessage(), 403));
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<?> authError(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(error(e.getMessage(), 401));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error("Internal server error", 500));
    }

    private Map<String, Object> error(String message, int status) {
        return Map.of("error", message, "status", status, "timestamp", Instant.now().toString());
    }
}
