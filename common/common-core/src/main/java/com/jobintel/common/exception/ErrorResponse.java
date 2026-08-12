package com.jobintel.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    Instant timestamp;
    int status;
    String errorCode;
    String message;
    String path;
    String correlationId;
    List<FieldError> validationErrors;

    @Value
    @Builder
    public static class FieldError {
        String field;
        String message;
        Object rejectedValue;
    }
}
