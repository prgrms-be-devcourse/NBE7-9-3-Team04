package com.backend.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import java.nio.charset.StandardCharsets
import java.util.*

//Toss payment api 통신을 위한 HTTP 클라이언트 설정
@Configuration
class WebClientConfig(
    @Value("\${toss.payments.secret-key}") private val secretKey: String,
    @Value(("\${toss.payments.base-url}")) private val baseUrl: String
) {

    @Bean
    fun webClient(): WebClient {
        val encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").toByteArray(StandardCharsets.UTF_8))
        val authHeader = "Basic $encodedAuth"

        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(ReactorClientHttpConnector())
            .defaultHeader(HttpHeaders.AUTHORIZATION, authHeader)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}
