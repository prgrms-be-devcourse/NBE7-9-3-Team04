package com.backend.global.ai.handler

import lombok.RequiredArgsConstructor
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.cglib.core.internal.Function
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
@RequiredArgsConstructor
class AiRequestHandler(
    @Value("\${openai.url}")
    private val apiUrl: String,
    @Value("\${spring.ai.openai.api-key}")
    private val apiKey: String,
    private val restClient: RestClient,
    private val openAiChatModel: OpenAiChatModel

) {

    fun <T> execute(mapper: (ChatClient) -> T): T {
        val chatClient = ChatClient.create(openAiChatModel)
        return mapper(chatClient)
    }

    fun <T> connectionAi(request: T): String? {
        return restClient.post()
            .uri(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .body(request!!)
            .retrieve()
            .body(String::class.java)
    }
}
