package com.qring.message.application.global.exception;

import lombok.Getter;

@Getter
public class MessageException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;

    public MessageException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

}
