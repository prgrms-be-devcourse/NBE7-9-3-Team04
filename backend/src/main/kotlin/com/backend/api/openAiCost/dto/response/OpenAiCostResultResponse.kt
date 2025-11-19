package com.backend.api.openAiCost.dto.response

data class OpenAiCostResultResponse(
    val `object`: String,
    val amount: OpenAiCostAmountResponse,
    val line_item: String?,
    val project_id: String?,
    val organization_id: String?
)