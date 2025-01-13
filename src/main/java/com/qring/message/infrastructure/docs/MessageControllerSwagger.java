package com.qring.message.infrastructure.docs;

import com.qring.message.application.global.dto.ResDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Message", description = "생성, 조회, 검색, 삭제 관련 메시지 API")
public interface MessageControllerSwagger {

    @Operation(summary = "메시지 검색", description = "동적 조건을 기준으로 메시지를 검색하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 검색 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "메시지 검색 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @GetMapping
    ResponseEntity<ResDTO<MessageSearchResDTOV1>> searchBy(@RequestHeader("X-Passport-Token") String passport,
                                                           @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                           @RequestParam(name = "userId", required = false) Long massageUserId,
                                                           @RequestParam(name = "id", required = false) Long id,
                                                           @RequestParam(name = "sort", required = false) String sort);
}