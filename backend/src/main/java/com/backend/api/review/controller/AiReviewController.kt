package com.backend.api.review.controller;

import com.backend.api.review.dto.response.AiReviewResponse;
import com.backend.api.review.service.AiReviewService;
import com.backend.domain.user.entity.User;
import com.backend.global.Rq.Rq;
import com.backend.global.dto.response.ApiResponse;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolio-review")
@RequiredArgsConstructor
@Tag(name = "Portfolio Ai Feedback", description = "포트폴리오 AI 첨삭")
public class AiReviewController {

    private final AiReviewService aiReviewService;
    private final Rq rq;

    @PostMapping
    @Operation(summary = "포트폴리오 AI 첨삭 생성", description = "사용자의 포트폴리오를 바탕으로 AI 첨삭을 생성합니다.")
    public ApiResponse<AiReviewResponse> createResumeFeedback() throws JsonProcessingException {
        User user = rq.getUser();
        AiReviewResponse response = aiReviewService.createAiReview(user);
        return ApiResponse.created("포트폴리오 AI 첨삭이 완료되었습니다.", response);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "AI 첨삭 단건 조회", description = "ID를 사용하여 특정 AI 첨삭 내용을 조회합니다.")
    public ApiResponse<AiReviewResponse> getReviewById(@PathVariable(required = false) String reviewId) {
        try {
            // reviewId가 null 또는 undefined일 경우 처리
            if (reviewId == null || reviewId.equals("undefined")) {
                throw new ErrorException(ErrorCode.INVALID_PARAMETER);
            }

            Long validReviewId = Long.parseLong(reviewId); // String -> Long 변환
            User user = rq.getUser();
            AiReviewResponse response = aiReviewService.findReviewById(validReviewId, user);
            return ApiResponse.ok("AI 첨삭 조회가 완료되었습니다.", response);
        } catch (NumberFormatException e) {
            throw new ErrorException(ErrorCode.INVALID_PARAMETER);        }
    }

    @GetMapping("/reviews")
    @Operation(summary = "AI 첨삭 다건 조회", description = "로그인한 사용자의 모든 AI 첨삭 목록을 최신순으로 조회합니다.")
    public ApiResponse<List<AiReviewResponse>> getMyReviews() {
        User user = rq.getUser();
        List<AiReviewResponse> responses = aiReviewService.findMyAiReviews(user);
        return ApiResponse.ok("내 AI 첨삭 목록 조회가 완료되었습니다.", responses);
    }
}