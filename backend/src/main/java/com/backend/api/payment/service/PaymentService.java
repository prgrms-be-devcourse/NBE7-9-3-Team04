package com.backend.api.payment.service;


import com.backend.api.payment.dto.request.PaymentRequest;
import com.backend.api.payment.dto.response.PaymentConfirmResponse;
import com.backend.api.payment.dto.response.PaymentResponse;
import com.backend.domain.payment.entity.Payment;
import com.backend.domain.payment.entity.PaymentStatus;
import com.backend.domain.payment.repository.PaymentRepository;
import com.backend.domain.user.entity.User;
import com.backend.global.Rq.Rq;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebClient webClient;
    private final Rq rq;


    //결제 승인
    @Transactional
    public PaymentResponse confirmPayment(PaymentRequest request){
        User user = rq.getUser();
        PaymentConfirmResponse response = webClient.post()
                .uri("/confirm") // 결제 승인 API 호출
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody ->
                                        Mono.error(new ErrorException(errorBody, ErrorCode.PAYMENT_APPROVE_FAILED))
                                )
                )
                .bodyToMono(PaymentConfirmResponse.class)
                .block();


        Payment payment = Payment.builder()
                .orderId(response.orderId)
                .paymentKey(response.paymentKey)
                .orderName(response.orderName)
                .method(response.method)
                .totalAmount(response.totalAmount)
                .status(PaymentStatus.DONE)
                .approvedAt(LocalDateTime.now())
                .user(user)
                .build();
        paymentRepository.save(payment);

        return PaymentResponse.from(payment);

    }

    @Transactional(readOnly = true)
    public PaymentResponse geyPaymentByKey(String paymentKey) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new ErrorException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ErrorException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.from(payment);
    }

    @Transactional
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }

}
