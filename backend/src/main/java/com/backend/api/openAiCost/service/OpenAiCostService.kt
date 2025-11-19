package com.backend.api.openAiCost.service

import com.backend.api.openAiCost.dto.request.OpenAiCostRequest
import com.backend.api.openAiCost.dto.response.CostSummaryResponse
import com.backend.api.openAiCost.dto.response.DailyCostReportResponse
import com.backend.api.openAiCost.dto.response.OpenAiCostResponse
import com.backend.api.openAiCost.dto.response.OpenAiCostResultResponse
import com.backend.global.ai.handler.AiAdminRequestHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@Service
class OpenAiCostService(
    private val aiAdminRequestHandler: AiAdminRequestHandler,
    private val objectMapper: ObjectMapper
) {

    private val kst = ZoneId.of("Asia/Seoul")

    //시간 변환(OpenAI가 요구하는 epoch second)
    private fun kstStartOfDayToUtcEpoch(date: LocalDate): Long {
        return date.atStartOfDay(kst).toInstant().epochSecond
    }

    private fun nextDayUtcEpoch(): Long {
        val tomorrow = LocalDate.now(kst).plusDays(1)
        return tomorrow.atStartOfDay(kst).toInstant().epochSecond
    }

    private fun buildQuery(request: OpenAiCostRequest, nextPage: String?): String {
        val base = request.toQueryString()
        return if (nextPage == null) {
            "?$base"
        } else {
            "?$base&page=$nextPage"
        }
    }

    fun fetchAllCosts(request: OpenAiCostRequest): List<OpenAiCostResultResponse> {
        val results = mutableListOf<OpenAiCostResultResponse>()

        var nextPage: String? = null

        do {
            val query = buildQuery(request, nextPage)
            val json = aiAdminRequestHandler.getCosts(query)

            val response = objectMapper.readValue(json, OpenAiCostResponse::class.java)

            response.data.forEach { bucket ->
                results.addAll(bucket.results)
            }

            nextPage = response.next_page
        } while (response.has_more)

        return results
    }

    private fun summarize(results: List<OpenAiCostResultResponse>): CostSummaryResponse {
        val byModel = mutableMapOf<String, Double>()
        var total = 0.0

        results.forEach { item ->
            val key = item.line_item ?: "Unknown"
            val value = item.amount.value

            total += value
            byModel[key] = (byModel[key] ?: 0.0) + value
        }

        return CostSummaryResponse(
            totalCost = total,
            byModel = byModel
        )
    }

    fun getTodayCost(): CostSummaryResponse {
        val today = LocalDate.now(kst)

        val request = OpenAiCostRequest(
            startTime = kstStartOfDayToUtcEpoch(today),
            endTime = nextDayUtcEpoch(),
            groupBy = "line_item"
        )

        val results = fetchAllCosts(request)
        return summarize(results)
    }

    fun getWeeklyCost(): Double {
        val monday = LocalDate.now(kst).with(DayOfWeek.MONDAY)

        val request = OpenAiCostRequest(
            startTime = kstStartOfDayToUtcEpoch(monday),
            endTime = nextDayUtcEpoch()
        )

        val results = fetchAllCosts(request)
        return results.sumOf { it.amount.value }
    }


    fun getMonthlyCost(): Double {
        val firstDay = LocalDate.now(kst).withDayOfMonth(1)

        val request = OpenAiCostRequest(
            startTime = kstStartOfDayToUtcEpoch(firstDay),
            endTime = nextDayUtcEpoch()
        )

        val results = fetchAllCosts(request)
        return results.sumOf { it.amount.value }
    }

    fun buildDailyReport(): DailyCostReportResponse {
        val today = getTodayCost()
        val weekly = getWeeklyCost()
        val monthly = getMonthlyCost()

        return DailyCostReportResponse(
            todayCost = today,
            weeklyCost = weekly,
            monthlyCost = monthly
        )
    }


}