package com.backend.api.question.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "AI 응답 값")
@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatGptResponse(
    @field:JsonProperty("choices")
    @field:Schema(description = "전체 값")
    val choiceResponses: List<ChoiceResponse>
)

