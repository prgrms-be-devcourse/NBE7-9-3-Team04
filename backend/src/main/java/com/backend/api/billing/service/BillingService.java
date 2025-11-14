package com.backend.api.billing.service;


import com.backend.api.billing.dto.request.BillingRequest;
import com.backend.api.billing.dto.response.BillingPaymentResponse;
import com.backend.api.billing.dto.response.BillingResponse;
import com.backend.api.payment.dto.response.PaymentResponse;
import com.backend.api.payment.service.PaymentService;
import com.backend.api.subscription.dto.response.SubscriptionResponse;
import com.backend.api.subscription.service.SubscriptionService;
import com.backend.domain.payment.entity.Payment;
import com.backend.domain.payment.entity.PaymentStatus;
import com.backend.domain.subscription.entity.Subscription;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BillingService {

    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final WebClient webClient;

    //빌링키 발급
    @Transactional
    public BillingResponse issueBillingKey(BillingRequest request) {

        if(request.authKey() == null){
            throw new ErrorException(ErrorCode.INVALID_AUTH_KEY);
        }

        if(request.customerKey() == null){
            throw new ErrorException(ErrorCode.INVALID_CUSTOMER_KEY);
        }

        Map<String, Object> billingResponse = webClient.post()
                .uri("v1/billing/authorizations/issue")
                .bodyValue(Map.of(
                        "authKey", request.authKey(),
                        "customerKey", request.customerKey()
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        String billingKey = (String) billingResponse.get("billingKey");

        if(billingKey == null){
            throw new ErrorException(ErrorCode.BILLING_KEY_NOT_FOUND);
        }

        // 구독 PREMIUM 전환
        SubscriptionResponse updated = subscriptionService.activatePremium(request.customerKey(), billingKey);

        //카드 등록 시 첫번째 결제 바로 실행
        Subscription subscription = subscriptionService.getSubscriptionByCustomerKey(request.customerKey());
        autoPayment(subscription);

        return new BillingResponse(billingKey, updated.customerKey());
    }

    //자동 결제. 한달 주기로 결제 진행
    @Transactional
    public void autoPayment(Subscription subscription){

        Subscription managedSub = subscriptionService.getSubscriptionById(subscription.getId());

        if (subscription.getBillingKey() == null) {
            throw new ErrorException(ErrorCode.BILLING_KEY_NOT_FOUND);
        }

        if (!subscription.getActive()) {
            throw new ErrorException(ErrorCode.SUBSCRIPTION_INACTIVE);
        }

        BillingPaymentResponse response = webClient.post()
                .uri("/v1/billing/" + subscription.getBillingKey())
                .bodyValue(Map.of(
                        "customerKey", subscription.getCustomerKey(),
                        "amount", subscription.getPrice(),
                        "orderId", UUID.randomUUID().toString(),
                        "orderName", "프리미엄 구독 자동결제",
                        "customerEmail", subscription.getUser().getEmail(),
                        "customerName", subscription.getUser().getName()
                ))
                .retrieve()
                .bodyToMono(BillingPaymentResponse.class)
                .block();

        LocalDateTime approvedAt = LocalDateTime.parse(response.approvedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Payment payment = Payment.builder()
                .orderId(response.orderId())
                .paymentKey(response.paymentKey())
                .orderName(response.orderName())
                .totalAmount(subscription.getPrice())
                .method("CARD")
                .status(PaymentStatus.valueOf(response.status()))
                .approvedAt(approvedAt)
                .user(subscription.getUser())
                .subscription(subscription)
                .build();

        paymentService.savePayment(payment);

        subscriptionService.updateNextBillingDate(managedSub, LocalDate.now().plusMonths(1));
        PaymentResponse.from(payment);
    }

}
