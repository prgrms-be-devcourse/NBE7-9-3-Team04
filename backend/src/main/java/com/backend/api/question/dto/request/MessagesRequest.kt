package com.backend.api.question.dto.request

import io.swagger.v3.oas.annotations.media.Schema


data class MessagesRequest(
    @field:Schema(description = "프롬프트 작성 규칙")
    val role: String,

    @field:Schema(description = "프롬프트 내용")
    val content: String
) {
    companion object {
        fun of(role: String, content: String): MessagesRequest {
            return MessagesRequest(role, content)
        }
    }
}
