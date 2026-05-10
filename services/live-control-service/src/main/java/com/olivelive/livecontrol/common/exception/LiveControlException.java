package com.olivelive.livecontrol.common.exception;

import lombok.Getter;

@Getter
public class LiveControlException extends RuntimeException {

    private final ErrorCode errorCode;

    public LiveControlException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
