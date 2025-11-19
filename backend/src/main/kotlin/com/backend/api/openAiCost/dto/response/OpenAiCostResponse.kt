package com.backend.api.openAiCost.dto.response

data class OpenAiCostResponse(
    val `object`: String,
    val has_more: Boolean,
    val next_page: String?,
    val data: List<OpenAiCostBucketResponse>
)