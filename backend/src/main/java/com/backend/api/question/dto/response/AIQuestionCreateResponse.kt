package com.backend.api.question.dto.response

import java.util.*

data class AIQuestionCreateResponse(
    val groudId: UUID?,
    val questions: List<AiQuestionResponse>
) {
    companion object {
        fun from(groudId: UUID?, responses: List<AiQuestionResponse>): AIQuestionCreateResponse {
            return AIQuestionCreateResponse(groudId, responses)
        }
    }
}
