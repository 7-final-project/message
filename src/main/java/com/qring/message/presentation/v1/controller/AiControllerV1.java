package com.qring.message.presentation.v1.controller;

import com.qring.message.application.global.dto.ResDTO;
import com.qring.message.application.v1.res.AiPostResDTOV1;
import com.qring.message.domain.model.AiEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/ais")
public class AiControllerV1 {

    @PostMapping
    public ResponseEntity<ResDTO<AiPostResDTOV1>> postBy() {

        // 더미데이터 ----------------------------------------------
        AiEntity dummyAiEntity = AiEntity.builder()
                .question("양식 카테고리의 음식점 추천해주세요.")
                .answer("파스타집")
                .build();
        // 추후 삭제 ----------------------------------------------

        return new ResponseEntity<>(
                ResDTO.<AiPostResDTOV1>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("AI 생성에 성공했습니다.")
                        .data(AiPostResDTOV1.of(dummyAiEntity))
                        .build(),
                HttpStatus.CREATED
        );
    }
}