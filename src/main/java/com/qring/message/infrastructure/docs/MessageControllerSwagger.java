package com.qring.message.infrastructure.docs;

import com.qring.message.application.global.ResDTO;
import com.qring.message.application.v1.res.MessageGetByIdResDTOV1;
import com.qring.message.application.v1.res.MessagePostResDTOV1;
import com.qring.message.application.v1.res.MessageSearchResDTOV1;
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

@Tag(name = "Message", description = "생성, 조회, 검색, 삭제 관련 메시지 API")
public interface MessageControllerSwagger {

    @Operation(summary = "메시지 생성", description = "사용자의 ID 와 메시지 내용을 기준으로 메시지 생성하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "메시지 생성 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "메시지 생성 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @PostMapping
    ResponseEntity<ResDTO<MessagePostResDTOV1>> postBy(@RequestHeader("X-User-Id") Long userId);


    @Operation(summary = "메시지 검색", description = "동적 조건을 기준으로 메시지를 검색하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 검색 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "메시지 검색 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @GetMapping
    ResponseEntity<ResDTO<MessageSearchResDTOV1>> searchBy(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                           @RequestParam(name = "userId", required = false) Long userId,
                                                           @RequestParam(name = "content", required = false) String content,
                                                           @RequestParam(name = "sort", required = false) String sort);


    @Operation(summary = "메시지 상세 조회", description = "메시지 ID 를 기준으로 메시지를 상세 조회하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 조회 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "메시지 조회 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @GetMapping("/{slackId}")
    ResponseEntity<ResDTO<MessageGetByIdResDTOV1>> getBy(@PathVariable Long slackId);


    @Operation(summary = "메시지 삭제", description = "사용자의 ID 와 메시지 ID 를 기준으로 메시지를 삭제하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 삭제 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "메시지 삭제 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @DeleteMapping("/{slackId}")
    ResponseEntity<ResDTO<Object>> deleteBy(@RequestHeader("X-User-Id") Long userId, @PathVariable Long slackId);
}
