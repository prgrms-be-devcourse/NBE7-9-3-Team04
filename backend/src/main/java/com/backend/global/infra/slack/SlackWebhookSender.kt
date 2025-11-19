package com.backend.global.infra.slack

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class SlackWebhookSender(
    @Value("\${slack.webhook.url}")
    private val slackWebhookUrl: String,

    private val restClient: RestClient
) {

    fun sendMessage(
        message: String,
        username: String = "OPEN AI API Cost Bot",
        iconEmoji: String? = ":robot_face:",
        iconUrl: String? = null
    ) {

        val payload = mutableMapOf<String, Any>(
            "username" to username,
            "text" to message
        )

        iconUrl?.let { payload["icon_url"] = it }
        if (iconEmoji != null) payload["icon_emoji"] = iconEmoji

        restClient.post()
            .uri(slackWebhookUrl)
            .body(payload)
            .retrieve()
            .toBodilessEntity()

    }
}
