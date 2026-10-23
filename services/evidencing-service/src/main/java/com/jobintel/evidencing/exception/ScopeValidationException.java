package com.jobintel.evidencing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ScopeValidationException extends RuntimeException {
    public ScopeValidationException(String message) {
        super(message);
    }
}
