package com.qring.message.presentation.controller;

import com.qring.message.application.global.dto.ResDTO;
import com.qring.message.application.res.MessageSearchResDTOV1;
import com.qring.message.application.service.MessageServiceV1;
import com.qring.message.infrastructure.docs.MessageControllerSwagger;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/messages")
public class MessageControllerV1 implements MessageControllerSwagger {

    private final MessageServiceV1 messageServiceV1;

    @GetMapping
    public ResponseEntity<ResDTO<MessageSearchResDTOV1>> searchBy(@RequestHeader("X-Passport-Token") String passport,
                                                                  @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @RequestParam(name = "userId", required = false) Long massageUserId,
                                                                  @RequestParam(name = "id", required = false) Long id,
                                                                  @RequestParam(name = "sort", required = false) String sort) {

        return new ResponseEntity<>(
                ResDTO.<MessageSearchResDTOV1>builder()
                        .code(HttpStatus.OK.value())
                        .message("메시지 검색에 성공했습니다.")
                        .data(messageServiceV1.searchBy(passport, pageable, massageUserId, id, sort))
                        .build(),
                HttpStatus.OK
        );
    }
}
