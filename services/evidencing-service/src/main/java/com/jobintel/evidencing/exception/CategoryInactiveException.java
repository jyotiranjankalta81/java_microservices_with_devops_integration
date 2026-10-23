package com.jobintel.evidencing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CategoryInactiveException extends RuntimeException {
    public CategoryInactiveException(String message) {
        super(message);
    }
}
