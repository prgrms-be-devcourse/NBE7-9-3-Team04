package com.backend.api.question.controller;

import com.backend.api.question.dto.response.AIQuestionCreateResponse;
import com.backend.api.question.dto.response.AiQuestionReadAllResponse;


import com.backend.api.question.dto.response.PortfolioListReadResponse;
import com.backend.api.question.service.AiQuestionService;

import com.backend.global.Rq.Rq;
import com.backend.global.dto.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/questions")
@RequiredArgsConstructor
@Tag(name = "AI Questions", description = "AI 관련 질문 관리 API")
public class AiQuestionController {
    private final AiQuestionService aiQuestionService;

    private final Rq rq;

    @PostMapping("")
    @Operation(summary = "AI 면접 질문 생성", description = "AI 질문을 생성합니다.")
    public ApiResponse<AIQuestionCreateResponse> createAiQuestion() throws JsonProcessingException {
        Long userId = rq.getUser().getId();
        AIQuestionCreateResponse responses = aiQuestionService.createAiQuestion(userId);
        return ApiResponse.created("AI 면접 질문이 완료되었습니다.",responses);
    }

    @GetMapping
    @Operation(summary = "AI 면접 질문 목록", description = "AI 질문 목록을 조회합니다.")
    public ApiResponse<AiQuestionReadAllResponse> readAllAiQuestion() {
        Long userId = rq.getUser().getId();
        AiQuestionReadAllResponse responses = aiQuestionService.readAllAiQuestion(userId);
        return ApiResponse.ok("AI 면접 질문 목록이 조회되었습니다.",responses);
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "그룹별 AI 면접 질문 목록", description = "그룹별 AI 질문 목록을 조회합니다.")
    public ApiResponse<PortfolioListReadResponse> readAiQuestion(
            @PathVariable UUID groupId
    ) {
        Long userId = rq.getUser().getId();
        PortfolioListReadResponse responses = aiQuestionService.readAiQuestion(userId,groupId);
        return ApiResponse.ok("그룹별 AI 면접 질문 목록이 조회되었습니다.",responses);
    }
}
