package com.olivelive.catalog.common.exception;

import lombok.Getter;

@Getter
public class CatalogException extends RuntimeException {

    private final ErrorCode errorCode;

    public CatalogException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
