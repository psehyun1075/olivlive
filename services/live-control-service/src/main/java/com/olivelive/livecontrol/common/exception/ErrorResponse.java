package com.olivelive.livecontrol.common.exception;

public record ErrorResponse(ErrorBody error) {

    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(new ErrorBody(code.name(), message));
    }

    public record ErrorBody(String code, String message) {}
}
