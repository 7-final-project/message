package com.qring.message.application.global.exception;

import lombok.Getter;

@Getter
public class UnauthorizedAccessException extends MessageException {
    public UnauthorizedAccessException(String message) {
        super(ErrorCode.AUTHORITY_ERROR, message);
    }
}
