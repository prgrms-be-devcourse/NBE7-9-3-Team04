package com.backend.api.billing.service

import com.backend.api.subscription.service.SubscriptionService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class BillingScheduler(
    private val billingService: BillingService,
    private val subscriptionService: SubscriptionService

) {

    private val log = LoggerFactory.getLogger(BillingScheduler::class.java)
    //00시에 자동으로 결제 진행
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    fun runAutoBillingTask() {
        log.info("[스케줄러 시작] 자동 결제 작업 시작 - {}", LocalDate.now())

        //오늘 결제일인 구독 목록 조회
        val subscriptions =
            subscriptionService.getActiveSubscriptionsByBillingDate(LocalDate.now())

        if (subscriptions.isEmpty()) {
            log.info("오늘 결제 대상 구독 없음")
            return
        }

        subscriptions.forEach { subscription ->
            if (subscription.billingKey == null) {
                subscription.deActivatePremium() //구독 종료
                subscriptionService.saveSubscription(subscription)
                return@forEach
            }
            billingService.autoPayment(subscription)
        }

        log.info("[스케줄러 완료] 자동 결제 작업 종료 - 총 {}건 처리", subscriptions.size)
    }
}

