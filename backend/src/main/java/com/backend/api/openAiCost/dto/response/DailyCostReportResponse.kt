package com.backend.api.openAiCost.dto.response

data class DailyCostReportResponse(
    val todayCost: CostSummaryResponse,
    val weeklyCost: Double,
    val monthlyCost: Double
)