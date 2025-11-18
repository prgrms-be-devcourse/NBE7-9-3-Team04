package com.backend.api.openAiCost.dto.response

data class CostSummaryResponse(
    val totalCost: Double,
    val byModel: Map<String, Double>
)