package com.backend.api.feedback.dto.response

import com.backend.domain.feedback.entity.Feedback


data class FeedbackReadResponse(
    val feedbackId: Long,
    val content: String,
    val score: Int
) {
    companion object {
        fun from(feedback: Feedback): FeedbackReadResponse {
            return FeedbackReadResponse(feedback.id, feedback.content, feedback.aiScore)
        }
    }
}
