package com.backend.global.infra.slack

import org.springframework.stereotype.Component

@Component
class SlackMessageFormatter {

    fun formatCostReport(
        todayCost: Double,
        modelCosts: Map<String, Double>,
        weeklyCost: Double,
        monthlyCost: Double
    ): String {

        val grouped = groupModelCosts(modelCosts)

        val sb = StringBuilder()
        sb.append("📊 *비용 리포트*\n\n")
        sb.append("● *오늘의 사용 비용*: $${"%.2f".format(todayCost)} USD\n\n")

        // 모델별 그룹 출력
        grouped.forEach { (modelName, group) ->
            sb.append("  • *$modelName*\n")
            if (group.input != null) sb.append("    ▪ input: $${"%.2f".format(group.input)}\n")
            if (group.output != null) sb.append("    ▪ output: $${"%.2f".format(group.output)}\n")

            group.other.forEach { (key, cost) ->
                sb.append("    ▪ $key: $${"%.2f".format(cost)}\n")
            }

            sb.append("\n")
        }

        sb.append("📅 *이번 주 누적 비용*: $${"%.2f".format(weeklyCost)} USD\n")
        sb.append("📆 *이번 달 누적 비용*: $${"%.2f".format(monthlyCost)} USD\n")

        return sb.toString()
    }

    //모델 grouping (input/output 묶기)
    private fun groupModelCosts(modelCosts: Map<String, Double>): Map<String, ModelCostGroup> {
        val grouped = mutableMapOf<String, ModelCostGroup>()

        modelCosts.forEach { (key, cost) ->
            val (modelName, type) = parseLineItem(key)
            val group = grouped.getOrPut(modelName) { ModelCostGroup() }

            when (type) {
                "input" -> group.input = cost
                "output" -> group.output = cost
                else -> group.other[key] = cost
            }
        }

        return grouped
    }

    private fun parseLineItem(lineItem: String): Pair<String, String?> {
        return if (lineItem.contains(",")) {
            val parts = lineItem.split(",").map { it.trim() }
            parts[0] to parts.getOrNull(1)
        } else {
            lineItem to null
        }
    }

    data class ModelCostGroup(
        var input: Double? = null,
        var output: Double? = null,
        val other: MutableMap<String, Double> = mutableMapOf()
    )
}