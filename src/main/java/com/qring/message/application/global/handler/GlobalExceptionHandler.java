package com.qring.message.application.global.handler;

import com.qring.message.application.global.dto.ResDTO;
import com.qring.message.application.global.exception.MessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MessageException.class})
    public ResponseEntity<ResDTO<Object>> MessageExceptionHandler(MessageException ex) {
        return new ResponseEntity<>(
                ResDTO.builder()
                        .code(ex.getErrorCode().getCode())
                        .message(ex.getMessage())
                        .build(),
                ex.getErrorCode().getHttpStatus()
        );
    }
}
