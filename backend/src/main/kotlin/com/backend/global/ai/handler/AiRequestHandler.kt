package com.backend.global.ai.handler

import com.backend.api.feedback.dto.response.AiFeedbackResponse
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class AiRequestHandler(
    @Value("\${openai.url}")
    private val apiUrl: String,
    @Value("\${spring.ai.openai.api-key}")
    private val apiKey: String,
    private val restClient: RestClient,
    private val openAiChatModel: OpenAiChatModel,
    private val logger: Logger = LoggerFactory.getLogger(AiRequestHandler::class.java),
    private val geminiModel: VertexAiGeminiChatModel
) {
    @Retry(name = "aiRetry")
    @CircuitBreaker(name = "aiExternal", fallbackMethod = "fallbackToGemini")
    @RateLimiter(name =  "rateLimiterAI", fallbackMethod = "rateLimiterFeedbackFallback")
    fun <T> execute(mapper: (ChatClient) -> T): T {
        val chatClient = ChatClient.create(openAiChatModel)
        return mapper(chatClient)
    }

    fun rateLimiterFeedbackFallback(prompt: Prompt, e: Throwable): AiFeedbackResponse {
        throw ErrorException(ErrorCode.RATE_LIMIT_EXCEEDED)
    }

    fun fallbackToGemini(prompt: Prompt, e: Throwable): AiFeedbackResponse {
        val geminiClient = ChatClient.create(geminiModel)
        logger.warn("[Fallback Triggered] OpenAI unavailable → switching to Gemini. Cause=${e.message}",e)
        return geminiClient.prompt(prompt)
            .call()
            .entity(AiFeedbackResponse::class.java)
            ?: throw ErrorException(ErrorCode.AI_SERVICE_ERROR)
    }

    @Retry(name = "aiRetry")
    fun <T : Any> connectionAi(request: T): String {
        return restClient.post()
            .uri(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .body(request)
            .retrieve()
            .body(String::class.java)
            ?: throw ErrorException(ErrorCode.AI_SERVICE_ERROR)
    }

    fun rateLimiterFallbackConnectionAi(e: Throwable): String {
        throw ErrorException(ErrorCode.RATE_LIMIT_EXCEEDED)
    }

    fun fallbackCircuitBreaker(e: Throwable): String {
        throw ErrorException(ErrorCode.AI_SERVICE_ERROR)
    }
}
