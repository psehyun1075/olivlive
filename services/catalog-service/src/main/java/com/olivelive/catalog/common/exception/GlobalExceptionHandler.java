package com.olivelive.catalog.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CatalogException.class)
    public ResponseEntity<ErrorResponse> handleCatalogException(CatalogException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case INVALID_REQUEST, INVALID_PRODUCT_STATUS -> HttpStatus.BAD_REQUEST;
            case PRODUCT_NOT_FOUND, LIVE_SESSION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case LIVE_SESSION_NOT_ATTACHABLE, DUPLICATE_LIVE_PRODUCT, DUPLICATE_DISPLAY_ORDER -> HttpStatus.CONFLICT;
            case LIVE_CONTROL_UNAVAILABLE -> HttpStatus.BAD_GATEWAY;
        };
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST, message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.DUPLICATE_LIVE_PRODUCT, "Duplicate live product mapping"));
    }
}
