package com.jobintel.common.exception;

import com.jobintel.common.web.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class OptimisticLockExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex,
                                                              HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.CONFLICT.value())
                .errorCode("OPTIMISTIC_LOCK_CONFLICT")
                .message("Job was modified by another request. Refresh and retry.")
                .path(request.getRequestURI())
                .correlationId(CorrelationIdFilter.getCorrelationId())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
