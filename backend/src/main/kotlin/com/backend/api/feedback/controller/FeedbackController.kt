package com.backend.api.feedback.controller

import com.backend.api.feedback.dto.response.FeedbackReadResponse
import com.backend.api.feedback.service.FeedbackService
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/feedback")
@Tag(name = "FeedbackController", description = "면접 답변 피드백 API")
class FeedbackController(
    private val feedbackService: FeedbackService,
    private val rq: Rq
) {
    @GetMapping("/{questionId}")
    @Operation(summary = "피드백 단건 조회", description = "답변 피드백 단건 조회합니다.")
    fun getFeedback(
        @PathVariable questionId: Long
    ): ApiResponse<FeedbackReadResponse> {
        val user = rq.getUser()
        val response = feedbackService.readFeedback(questionId, user)
        return ApiResponse.ok("피드백 단건 조회합니다.", response)
    }
}
