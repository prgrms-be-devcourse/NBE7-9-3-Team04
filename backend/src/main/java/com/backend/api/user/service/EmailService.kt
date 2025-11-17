package com.backend.api.user.service

import com.backend.domain.user.entity.AccountStatus
import com.backend.domain.user.entity.User
import com.backend.domain.user.entity.VerificationCode
import com.backend.domain.user.repository.VerificationCodeRepository
import com.backend.domain.userPenalty.entity.UserPenalty
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
class EmailService(
    private val verificationCodeRepository: VerificationCodeRepository,

    @Value("\${mailgun.api-key}")
    private val mailgunApiKey: String,

    @Value("\${mailgun.domain}")
    private val mgDomain: String,

    @Value("\${mailgun.from}")
    private val fromEmail: String
) {

    private val log = LoggerFactory.getLogger(EmailService::class.java)

    private val CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private val CODE_LENGTH = 6

    private val mailgunClient: WebClient = WebClient.builder()
        .baseUrl("https://api.mailgun.net/v3/$mgDomain")
        .defaultHeaders {
            val auth = "api:$mailgunApiKey"
            val encoded = Base64.getEncoder().encodeToString(auth.toByteArray())
            it.set("Authorization", "Basic $encoded")
        }
        .build()

    // 메일 발송
    private fun sendEmail(toEmail: String, subject: String, content: String) {
        try {
            val form = LinkedMultiValueMap<String, String>().apply {
                add("from", fromEmail)
                add("to", toEmail)
                add("subject", subject)
                add("text", content)
            }

            val response = mailgunClient.post()
                .uri("/messages")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String::class.java)
                .block()

            log.info("메일 전송 완료: {} | 응답={}", toEmail, response)

        } catch (e: Exception) {
            log.error("메일 전송 실패: {}", e.message)
            throw ErrorException(ErrorCode.EMAIL_SEND_FAILED)
        }
    }


    // 인증코드 생성 및 발송
    @Transactional
    fun createAndSendVerificationCode(email: String) {
        verificationCodeRepository.findByEmail(email)?.let {
            verificationCodeRepository.delete(it)
        }

        val code = generateVerificationCode()

        val verification = VerificationCode(
            email = email,
            code = code,
            expiresAt = LocalDateTime.now().plusMinutes(5),
            verified = false
        )

        verificationCodeRepository.save(verification)

        val content = """
            안녕하세요. Dev-Station 입니다.

            아래 인증 코드를 입력해 주세요.

            인증코드: $code
            (유효시간 5분)
        """.trimIndent()

        sendEmail(email, "[Dev-Station] 이메일 인증 코드", content)
    }


    private fun generateVerificationCode(): String {
        val random = SecureRandom()
        return buildString(CODE_LENGTH) {
            repeat(CODE_LENGTH) {
                append(CHAR_SET[random.nextInt(CHAR_SET.length)])
            }
        }
    }


    // 인증코드 검증
    fun verifyCode(email: String, code: String) {
        val verification = verificationCodeRepository.findByEmail(email)
            ?: throw ErrorException(ErrorCode.INVALID_VERIFICATION_CODE)

        if (verification.isExpired())
            throw ErrorException(ErrorCode.EXPIRED_VERIFICATION_CODE)

        if (verification.code != code)
            throw ErrorException(ErrorCode.INVALID_VERIFICATION_CODE)

        verification.markAsVerified()
        verificationCodeRepository.save(verification)

        log.info("[이메일 인증] 인증 성공: {}", email)
    }

    fun isVerified(email: String): Boolean =
        verificationCodeRepository.findByEmail(email)?.verified ?: false


    // 회원 가입 환영 메일
    fun sendWelcomeMail(user: User) {
        val content = """
            안녕하세요, ${user.name}님 👋

            Dev-Station 가입을 환영합니다!
        """.trimIndent()

        sendEmail(user.email, "[Dev-Station] 회원가입을 환영합니다!", content)
    }


    //계정 상태 변경 메일
    fun sendStatusChangeMail(user: User, penalty: UserPenalty?) {

        val (subject, content) = when (user.accountStatus) {

            AccountStatus.SUSPENDED -> {
                val reason = penalty?.reason ?: "사유 정보 없음"
                val endAt = penalty?.endAt ?: "미정"

                "[Dev-Station] 계정 일시정지 안내" to """
                    안녕하세요, ${user.name}님.

                    회원님의 계정이 일시정지되었습니다.
                    사유: $reason
                    종료일: $endAt
                """.trimIndent()
            }

            AccountStatus.BANNED -> {
                val reason = penalty?.reason ?: "사유 정보 없음"
                "[Dev-Station] 계정 영구정지 안내" to """
                    안녕하세요, ${user.name}님.

                    회원님의 계정이 영구 정지되었습니다.
                    사유: $reason
                """.trimIndent()
            }

            AccountStatus.ACTIVE -> {
                "[Dev-Station] 계정 복구 안내" to """
                    안녕하세요, ${user.name}님.

                    회원님의 계정이 정상으로 복구되었습니다.
                """.trimIndent()
            }

            AccountStatus.DEACTIVATED -> {
                "[Dev-Station] 탈퇴 완료 안내" to """
                    안녕하세요, ${user.name}님.

                    회원님의 계정 탈퇴 처리가 완료되었습니다.
                """.trimIndent()
            }
        }

        sendEmail(user.email, subject, content)
    }


    // 임시 비밀번호 발송
    fun sendNewPassword(email: String, newPassword: String) {

        val content = """
            안녕하세요. Dev-Station 입니다.

            요청하신 임시 비밀번호를 발급해드립니다.
            로그인 후 반드시 새 비밀번호로 변경해주세요.

            임시 비밀번호: $newPassword
        """.trimIndent()

        sendEmail(email, "[Dev-Station] 임시 비밀번호 안내", content)
    }
}
