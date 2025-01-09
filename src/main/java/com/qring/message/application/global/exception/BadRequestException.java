package com.qring.message.application.global.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends MessageException {
    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST_ERROR, message);
    }
}
