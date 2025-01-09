package com.qring.message.application.global.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends MessageException {
    public EntityNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND_ERROR, message);
    }
}
