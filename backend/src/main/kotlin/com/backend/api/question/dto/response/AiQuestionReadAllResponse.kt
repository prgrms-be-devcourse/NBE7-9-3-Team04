package com.backend.api.question.dto.response


data class AiQuestionReadAllResponse(
    val questions: List<AiQuestionReadResponse>
) {
    companion object {
        fun from(responses: List<AiQuestionReadResponse>): AiQuestionReadAllResponse {
            return AiQuestionReadAllResponse(responses)
        }
    }
}