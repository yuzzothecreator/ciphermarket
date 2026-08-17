package com.ciphermarket.api.common.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemFactory.create(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                correlationId()
        );
    }

    @ExceptionHandler({com.ciphermarket.api.common.exception.AccessDeniedException.class, AccessDeniedException.class})
    public ProblemDetail handleAccessDenied(Exception ex) {
        return ProblemFactory.create(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                ex.getMessage() != null ? ex.getMessage() : "You do not have permission to perform this action",
                correlationId()
        );
    }

    @ExceptionHandler(TenantIsolationException.class)
    public ProblemDetail handleTenantIsolation(TenantIsolationException ex) {
        return ProblemFactory.create(
                HttpStatus.FORBIDDEN,
                "Tenant Isolation Violation",
                ex.getMessage(),
                correlationId()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ProblemFactory.create(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                detail,
                correlationId()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        return ProblemFactory.create(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                ex.getMessage(),
                correlationId()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return ProblemFactory.create(
                HttpStatus.BAD_REQUEST,
                "Invalid Request",
                ex.getMessage(),
                correlationId()
        );
    }

    private String correlationId() {
        String id = MDC.get("correlationId");
        return id != null ? id : "unknown";
    }
}
