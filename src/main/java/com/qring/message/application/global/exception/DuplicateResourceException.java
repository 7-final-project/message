package com.qring.message.application.global.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends MessageException {
    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_ERROR, message);
    }
}
