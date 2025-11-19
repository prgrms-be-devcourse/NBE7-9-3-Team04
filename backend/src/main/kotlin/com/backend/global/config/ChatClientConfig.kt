package com.backend.global.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ChatClientConfig {
    @Bean
    fun openAiChatClient(chatModel: OpenAiChatModel): ChatClient =
        ChatClient.create(chatModel)


    @Bean
    fun geminiChatClient(chatModel: VertexAiGeminiChatModel): ChatClient =
        ChatClient.create(chatModel)

}
