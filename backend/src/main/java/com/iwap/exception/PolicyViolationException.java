package com.iwap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class PolicyViolationException extends RuntimeException {
    public PolicyViolationException(String message) {
        super(message);
    }
}
