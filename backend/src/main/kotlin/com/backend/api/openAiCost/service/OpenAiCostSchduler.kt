package com.backend.api.openAiCost.service

import com.backend.global.infra.slack.SlackMessageFormatter
import com.backend.global.infra.slack.SlackWebhookSender
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OpenAiCostScheduler(
    private val openAiCostService: OpenAiCostService,
    private val slackMessageFormatter: SlackMessageFormatter,
    private val slackWebhookSender: SlackWebhookSender
) {

    private val log = LoggerFactory.getLogger(SlackWebhookSender::class.java)

    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Seoul")
    fun sendDailyCostReport() {
        val report = openAiCostService.buildDailyReport()

        val message = slackMessageFormatter.formatCostReport(
            todayCost = report.todayCost.totalCost,
            modelCosts = report.todayCost.byModel,
            weeklyCost = report.weeklyCost,
            monthlyCost = report.monthlyCost
        )

        slackWebhookSender.sendMessage(message)
        log.info("Slack Webhook 전송 완료!")
    }
}