package com.qring.message.presentation.v1.controller;

import com.qring.message.application.global.dto.ResDTO;
import com.qring.message.application.v1.res.AiGetByIdResDTOV1;
import com.qring.message.application.v1.res.AiPostResDTOV1;
import com.qring.message.application.v1.res.AiSearchResDTOV1;
import com.qring.message.domain.model.AiEntity;
import com.qring.message.infrastructure.docs.AiControllerSwagger;
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
@RequestMapping("/v1/ais")
public class AiControllerV1 implements AiControllerSwagger {

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

    @GetMapping
    public ResponseEntity<ResDTO<AiSearchResDTOV1>> searchBy(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                             @RequestParam(name = "question", required = false) String question,
                                                             @RequestParam(name = "answer", required = false) String answer,
                                                             @RequestParam(name = "sort", required = false) String sort) {

        // 더미데이터 ----------------------------------------------
        List<AiEntity> dummyAis = List.of(
                AiEntity.builder()
                        .question("양식 카테고리의 음식점 추천해주세요.")
                        .answer("파스타집")
                        .build(),
                AiEntity.builder()
                        .question("중식 카테고리의 음식점 추천해주세요.")
                        .answer("짬뽕집")
                        .build(),
                AiEntity.builder()
                        .question("일식 카테고리의 음식점 추천해주세요.")
                        .answer("초밥집")
                        .build()
        );

        Page<AiEntity> dummyPage = new PageImpl<>(dummyAis, pageable, dummyAis.size());
        // 추후 삭제 ----------------------------------------------

        return new ResponseEntity<>(
                ResDTO.<AiSearchResDTOV1>builder()
                        .code(HttpStatus.OK.value())
                        .message("AI 검색에 성공했습니다.")
                        .data(AiSearchResDTOV1.of(dummyPage))
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResDTO<AiGetByIdResDTOV1>> getBy(@PathVariable Long id) {

        // 더미데이터 ----------------------------------------------
        AiEntity dummyAiEntity = AiEntity.builder()
                .question("양식 카테고리의 음식점 추천해주세요.")
                .answer("파스타집")
                .build();
        // 추후 삭제 ----------------------------------------------

        return new ResponseEntity<>(
                ResDTO.<AiGetByIdResDTOV1>builder()
                        .code(HttpStatus.OK.value())
                        .message("AI 상세 조회에 성공했습니다.")
                        .data(AiGetByIdResDTOV1.of(dummyAiEntity))
                        .build(),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResDTO<Object>> deleteBy(@PathVariable Long id) {

        return new ResponseEntity<>(
                ResDTO.builder()
                        .code(HttpStatus.OK.value())
                        .message("AI 삭제에 성공했습니다.")
                        .build(),
                HttpStatus.OK
        );
    }
}