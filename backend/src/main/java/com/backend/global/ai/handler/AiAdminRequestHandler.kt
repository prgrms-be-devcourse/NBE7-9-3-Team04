package com.backend.global.ai.handler

import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class AiAdminRequestHandler(
    @Value("\${openai.admin.url}")
    private val adminApiUrl: String,

    @Value("\${openai.admin.api-key}")
    private val adminApiKey: String,

    private val restClient: RestClient
) {

    fun getCosts(query: String): String {
        val getUrl = "$adminApiUrl$query"

        return restClient.get()
            .uri(adminApiUrl)
            .header("Authorization", "Bearer $adminApiKey")
            .retrieve()
            .body(String::class.java)
            ?: throw ErrorException(ErrorCode.AI_SERVICE_ERROR)
    }


}