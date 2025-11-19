package com.backend.api.openAiCost.dto.request

data class OpenAiCostRequest(
    val startTime: Long,
    val endTime: Long?=null,
    val groupBy: String?=null,
) {

    fun toQueryString(): String {
        val params = mutableListOf("start_time=$startTime")

        endTime?.let { params.add("end_time=$endTime") }
        groupBy?.let { params.add("group_by=$groupBy") }

        return params.joinToString("&")
    }
}