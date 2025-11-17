package com.backend.api.review.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class AiReviewResponse(
    @field:Schema(description = "리뷰 ID", example = "1")
    val reviewId: Long,

    @field:Schema(description = "포트폴리오 AI 첨삭 (Markdown 형식)", example = "## 포트폴리오 분석 결과...")
    val feedbackContent: String,

    @field:Schema(description = "생성일", example = "2025-10-27T10:15:30")
    val createDate: LocalDateTime
) {
    companion object {
        fun of(reviewId: Long, feedbackContent: String, createDate: LocalDateTime): AiReviewResponse {
            return AiReviewResponse(reviewId, feedbackContent, createDate)
        }
    }
}