package com.backend.api.openAiCost.service

import com.backend.global.ai.handler.AiAdminRequestHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class OpenAiCostService(
    private val adminHanlder: AiAdminRequestHandler,
    private val objectMapper: ObjectMapper
) {
//
//    fun getCost(start: Long, end: Long)
}