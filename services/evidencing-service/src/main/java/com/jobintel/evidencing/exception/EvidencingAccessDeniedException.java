package com.jobintel.evidencing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class EvidencingAccessDeniedException extends RuntimeException {
    public EvidencingAccessDeniedException(String message) {
        super(message);
    }
}
