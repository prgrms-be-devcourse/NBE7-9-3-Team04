package com.backend.api.question.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "AI 응답 값 서브 내용")
@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageResponse(
    @field:JsonProperty("role")
    @field:Schema(description = "프롬프트 규칙")
    val role: String,
    @field:JsonProperty("content")
    @field:Schema(description = "프롬프트 내용")
    val content: String
)
