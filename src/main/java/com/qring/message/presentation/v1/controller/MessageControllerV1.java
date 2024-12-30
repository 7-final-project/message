package com.qring.message.presentation.v1.controller;

import com.qring.message.application.global.ResDTO;
import com.qring.message.application.v1.res.MessageGetByIdResDTOV1;
import com.qring.message.application.v1.res.MessagePostResDTOV1;
import com.qring.message.application.v1.res.MessageSearchResDTOV1;
import com.qring.message.domain.model.MessageEntity;
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
@RequestMapping("/v1/messages")
public class MessageControllerV1 {

    @PostMapping
    public ResponseEntity<ResDTO<MessagePostResDTOV1>> postBy(@RequestHeader("X-User-Id") Long userId) {

        // 더미데이터 ----------------------------------------------
        MessageEntity dummyMessageEntity = MessageEntity.builder()
                .userId(1L)
                .content("대기 등록이 완료 되었습니다."
                        + "예약 인원 : 2명"
                        + "대기 번호 : 30번"
                        + "내 앞 대기팀 : 2팀")
                .build();
        // 추후 삭제 ----------------------------------------------

        return new ResponseEntity<>(
                ResDTO.<MessagePostResDTOV1>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("메시지 생성에 성공했습니다.")
                        .data(MessagePostResDTOV1.of(dummyMessageEntity))
                        .build(),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ResDTO<MessageSearchResDTOV1>> searchBy(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @RequestParam(name = "userId", required = false) Long userId,
                                                                  @RequestParam(name = "content", required = false) String content,
                                                                  @RequestParam(name = "sort", required = false) String sort) {

        // 더미데이터 ----------------------------------------------
        List<MessageEntity> dummyMessages = List.of(
                MessageEntity.builder()
                        .userId(1L)
                        .content("대기 등록이 완료 되었습니다."
                                + "예약 인원 : 2명"
                                + "대기 번호 : 30번"
                                + "내 앞 대기팀 : 2팀")
                        .build(),
                MessageEntity.builder()
                        .userId(2L)
                        .content("대기 등록이 완료 되었습니다."
                                + "예약 인원 : 4명"
                                + "대기 번호 : 3번"
                                + "내 앞 대기팀 : 1팀")
                        .build(),
                MessageEntity.builder()
                        .userId(1L)
                        .content("대기 등록이 완료 되었습니다."
                                + "예약 인원 : 6명"
                                + "대기 번호 : 59번"
                                + "내 앞 대기팀 : 10팀")
                        .build()
        );

        Page<MessageEntity> dummyPage = new PageImpl<>(dummyMessages, pageable, dummyMessages.size());
        // 추후 삭제 ----------------------------------------------

        return new ResponseEntity<>(
                ResDTO.<MessageSearchResDTOV1>builder()
                        .code(HttpStatus.OK.value())
                        .message("메시지 검색에 성공했습니다.")
                        .data(MessageSearchResDTOV1.of(dummyPage))
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/{slackId}")
    public ResponseEntity<ResDTO<MessageGetByIdResDTOV1>> getBy(@PathVariable Long slackId) {

        // 더미데이터 ----------------------------------------------
        MessageEntity dummyMessageEntity = MessageEntity.builder()
                .userId(1L)
                .content("대기 등록이 완료 되었습니다."
                        + "예약 인원 : 2명"
                        + "대기 번호 : 30번"
                        + "내 앞 대기팀 : 2팀")
                .build();
        // 추후 삭제 ----------------------------------------------

        return new ResponseEntity<>(
                ResDTO.<MessageGetByIdResDTOV1>builder()
                        .code(HttpStatus.OK.value())
                        .message("메시지 상세 조회에 성공했습니다.")
                        .data(MessageGetByIdResDTOV1.of(dummyMessageEntity))
                        .build(),
                HttpStatus.OK
        );
    }
    @DeleteMapping("/{slackId}")
    public ResponseEntity<ResDTO<Object>> deleteBy(@RequestHeader("X-User-Id") Long userId,
                                                   @PathVariable Long slackId) {

        return new ResponseEntity<>(
                ResDTO.builder()
                        .code(HttpStatus.OK.value())
                        .message("메시지 삭제에 성공했습니다.")
                        .build(),
                HttpStatus.OK
        );
    }
}
