package com.qring.message.infrastructure.docs;

import com.qring.message.application.global.dto.ResDTO;
import com.qring.message.application.v1.res.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Ai", description = "생성, 조회, 검색, 삭제 관련 AI API")
@RequestMapping("/v1/ais")
public interface AiControllerSwagger {

    @Operation(summary = "AI 생성", description = "질문 내용을 기준으로 AI 답볍을 생성하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "AI 생성 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "AI 생성 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @PostMapping
    ResponseEntity<ResDTO<AiPostResDTOV1>> postBy();


    @Operation(summary = "AI 검색", description = "동적 조건을 기준으로 AI 내용을 검색하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI 검색 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "AI 검색 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @GetMapping
    ResponseEntity<ResDTO<AiSearchResDTOV1>> searchBy(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                      @RequestParam(name = "question", required = false) String question,
                                                      @RequestParam(name = "answer", required = false) String answer,
                                                      @RequestParam(name = "sort", required = false) String sort);


    @Operation(summary = "AI 상세 조회", description = "AI ID 를 기준으로 AI 내용을 상세 조회하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI 조회 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "AI 조회 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @GetMapping("/{aiId}")
    ResponseEntity<ResDTO<AiGetByIdResDTOV1>> getBy(@PathVariable Long aiId);


    @Operation(summary = "AI 삭제", description = "AI ID 를 기준으로 AI를 삭제하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI 삭제 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "AI 삭제 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @DeleteMapping("/{aiId}")
    ResponseEntity<ResDTO<Object>> deleteBy(@PathVariable Long aiId);
}
