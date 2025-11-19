package com.backend.api.openAiCost.dto.response

data class OpenAiCostBucketResponse(
    val `object`: String,
    val start_time: Long,
    val end_time: Long,
    val results: List<OpenAiCostResultResponse>

)