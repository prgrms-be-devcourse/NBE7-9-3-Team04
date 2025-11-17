package com.backend.api.user.event

import com.backend.api.user.event.publisher.UserSignupEvent
import com.backend.api.user.event.publisher.UserStatusChangeEvent
import com.backend.api.user.service.EmailService
import com.backend.domain.user.repository.VerificationCodeRepository
import com.backend.domain.userPenalty.entity.UserPenalty
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class EmailEventListener(
    private val emailService: EmailService,
    private val verificationCodeRepository: VerificationCodeRepository
) {

    private val log = LoggerFactory.getLogger(EmailEventListener::class.java)

    // 계정 상태 변경 시 이메일 발송
    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserStatusChange(event: UserStatusChangeEvent) {
        val user = event.user
        val penalty: UserPenalty? = event.penalty

        log.info(
            "[이메일 이벤트] 계정 상태 변경 감지: {}, 상태: {}, 사유: {}",
            user.email,
            user.accountStatus,
            penalty?.reason ?: "해당 없음"
        )

        emailService.sendStatusChangeMail(user, penalty)
    }

    // 회원가입 완료 후 인증 코드 삭제 + 환영 이메일 발송
    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserSignup(event: UserSignupEvent) {
        val user = event.user

        try {
            log.info("[이메일 이벤트] 회원가입 완료 감지: {}", user.email)

            // 인증 코드 삭제 — Kotlin null 기반 처리
            verificationCodeRepository.findByEmail(user.email)
                ?.let { verificationCodeRepository.delete(it) }

            // 환영 메일 발송
//            emailService.sendWelcomeMail(user)

//            log.info("[이메일 이벤트] 회원가입 환영 메일 전송 완료: {}", user.email)

        } catch (e: Exception) {
            log.error(
                "[이메일 이벤트] 회원가입 후 이메일 처리 실패 ({}): {}",
                user.email,
                e.message,
                e
            )
        }
    }
}
