package com.olivelive.livecontrol.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LiveControlException.class)
    public ResponseEntity<ErrorResponse> handleLiveControlException(LiveControlException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case LIVE_SESSION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case SESSION_NOT_READY, SESSION_NOT_LIVE -> HttpStatus.CONFLICT;
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
}
