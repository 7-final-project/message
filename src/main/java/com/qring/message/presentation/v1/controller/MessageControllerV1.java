package com.qring.message.presentation.v1.controller;

import com.qring.message.application.global.dto.ResDTO;
import com.qring.message.application.v1.res.MessageGetByIdResDTOV1;
import com.qring.message.application.v1.res.MessageSearchResDTOV1;
import com.qring.message.application.v1.service.MessageServiceV1;
import com.qring.message.domain.model.MessageEntity;
import com.qring.message.infrastructure.messaging.kafka.KafkaMessageConsumerV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/messages")
public class MessageControllerV1 {

    private final MessageServiceV1 messageServiceV1;

    // 삭제 예정
//    private final KafkaMessageConsumerV1 messageConsumerV1;
//
//    @PostMapping("/{email}")
//    public ResponseEntity<ResDTO<Object>> postBy(@PathVariable String email) {
//
//        messageConsumerV1.sendMessageToUser(email);
//
//        return new ResponseEntity<>(
//                ResDTO.builder()
//                        .code(HttpStatus.CREATED.value())
//                        .message("메시지 생성에 성공했습니다.")
//                        .build(),
//                HttpStatus.CREATED
//        );
//    }

    @GetMapping
    public ResponseEntity<ResDTO<MessageSearchResDTOV1>> searchBy(@RequestHeader("X-Passport-Token") String passport,
                                                                  @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @RequestParam(name = "userId", required = false) Long userId,
                                                                  @RequestParam(name = "id", required = false) Long id,
                                                                  @RequestParam(name = "sort", required = false) String sort) {


        return new ResponseEntity<>(
                ResDTO.<MessageSearchResDTOV1>builder()
                        .code(HttpStatus.OK.value())
                        .message("메시지 검색에 성공했습니다.")
                        .data(messageServiceV1.searchBy(passport, pageable, userId, id, sort))
                        .build(),
                HttpStatus.OK
        );
    }
}
