package com.backend.api.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AiReviewResponse(
        @Schema(description = "리뷰 ID", example = "1")
        Long reviewId,

        @Schema(description = "포트폴리오 AI 첨삭 (Markdown 형식)", example = "## 포트폴리오 분석 결과...")
        String feedbackContent,

        @Schema(description = "생성일", example = "2025-10-27T10:15:30")
        LocalDateTime createDate
) {
    public static AiReviewResponse of(Long reviewId, String feedbackContent, LocalDateTime createDate) {
        return new AiReviewResponse(reviewId, feedbackContent, createDate);
    }
}