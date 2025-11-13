package com.backend.api.user.service

import com.backend.domain.user.entity.AccountStatus
import com.backend.domain.user.entity.User
import com.backend.domain.user.entity.VerificationCode
import com.backend.domain.user.repository.VerificationCodeRepository
import com.backend.domain.userPenalty.entity.UserPenalty
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val verificationCodeRepository: VerificationCodeRepository
) {

    companion object {
        private val log = LoggerFactory.getLogger(EmailService::class.java)

        private const val CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        private const val CODE_LENGTH = 6
    }

    @Transactional
    fun createAndSendVerificationCode(email: String) {
        verificationCodeRepository.findByEmail(email)
            ?.let { verificationCodeRepository.delete(it) }

        val code = generateVerificationCode()

        val verification = VerificationCode.builder()
            .email(email)
            .code(code)
            .expiresAt(LocalDateTime.now().plusMinutes(5))
            .verified(false)
            .build()

        verificationCodeRepository.save(verification)

        sendVerificationMailAsync(email, code)
    }

    @Async("mailExecutor")
    fun sendVerificationMailAsync(email: String, code: String) {
        try {
            val message = SimpleMailMessage().apply {
                setTo(email)
                subject = "[Dev-Station] 이메일 인증 코드"
                text =
                    """
                    안녕하세요. Dev-Station 입니다.
                    
                    회원가입을 위해 아래 인증 코드를 입력해주세요.
                    인증코드: $code
                    
                    해당 코드는 5분간 유효합니다.
                    """.trimIndent()
            }

            mailSender.send(message)
            log.info("[이메일 인증] 인증코드 전송 완료: {}", email)
        } catch (e: Exception) {
            log.error("이메일 인증코드 전송 실패: {}", e.message, e)
            throw ErrorException(ErrorCode.EMAIL_SEND_FAILED)
        }
    }

    private fun generateVerificationCode(): String {
        val random = SecureRandom()
        return buildString(CODE_LENGTH) {
            repeat(CODE_LENGTH) {
                append(CHAR_SET[random.nextInt(CHAR_SET.length)])
            }
        }
    }

    fun verifyCode(email: String, code: String) {
        val verification = verificationCodeRepository.findByEmail(email)
            ?: throw ErrorException(ErrorCode.INVALID_VERIFICATION_CODE)

        if (verification.isExpired()) {
            throw ErrorException(ErrorCode.EXPIRED_VERIFICATION_CODE)
        }

        if (verification.code != code) {
            throw ErrorException(ErrorCode.INVALID_VERIFICATION_CODE)
        }

        verification.markAsVerified()
        verificationCodeRepository.save(verification)

        log.info("[이메일 인증] 인증 성공: {}", email)
    }

    fun isVerified(email: String): Boolean =
        verificationCodeRepository.findByEmail(email)?.verified ?: false

    @Async("mailExecutor")
    fun sendWelcomeMail(user: User) {
        try {
            val message = SimpleMailMessage().apply {
                setTo(user.email)
                subject = "[Dev-Station] 회원가입을 환영합니다!"
                text =
                    """
                    안녕하세요, ${user.name}님 👋
                    
                    Dev-Station에 가입해 주셔서 감사합니다.
                    지금부터 CS 인터뷰 문제 풀이, AI 피드백, 프로젝트 모집 등 모든 기능을 이용하실 수 있습니다.
                    
                    앞으로도 좋은 서비스로 보답하겠습니다!
                    
                    - Dev-Station 팀 드림 -
                    """.trimIndent()
            }

            mailSender.send(message)
            log.info("[회원가입 메일] 환영 메일 전송 완료: {}", user.email)
        } catch (e: Exception) {
            log.error("회원가입 환영 메일 전송 실패: {}", e.message, e)
        }
    }

    @Async("mailExecutor")
    fun sendStatusChangeMail(user: User, penalty: UserPenalty?) {

        val status = user.accountStatus

        // 정지/영구정지는 패널티 필수
        if ((status == AccountStatus.SUSPENDED || status == AccountStatus.BANNED) && penalty == null) {
            log.error("패널티가 있어야 하는 상태인데 penalty=null | user={}", user.getEmail())
            return
        }

        val (subject, content) = when (status) {

            AccountStatus.SUSPENDED -> {
                "[Dev-Station] 계정 일시정지 안내" to """
                안녕하세요, ${user.getName()}님.
                
                회원님의 계정이 일시정지되었습니다.
                사유: ${penalty!!.reason}
                종료일: ${penalty.endAt ?: "미정"}
            """.trimIndent()
            }

            AccountStatus.BANNED -> {
                "[Dev-Station] 계정 영구 정지 안내" to """
                안녕하세요, ${user.getName()}님.
                
                회원님의 계정이 영구 정지되었습니다.
                사유: ${penalty!!.reason}
            """.trimIndent()
            }

            AccountStatus.ACTIVE -> {
                "[Dev-Station] 계정 복구 안내" to """
                안녕하세요, ${user.getName()}님.
                
                회원님의 계정이 복구되었습니다.
            """.trimIndent()
            }

            AccountStatus.DEACTIVATED -> {
                "[Dev-Station] 탈퇴 완료 안내" to """
                안녕하세요, ${user.getName()}님.
                
                회원님의 계정 탈퇴 처리가 완료되었습니다.
            """.trimIndent()
            }

            else -> return
        }

        // 메일 발송
        val message = SimpleMailMessage().apply {
            setTo(user.getEmail())
            this.subject = subject
            text = content
        }

        mailSender.send(message)
    }

    fun sendNewPassword(email: String, newPassword: String) {
        try {
            val message = SimpleMailMessage().apply {
                setTo(email)
                subject = "[Dev-Station] 임시 비밀번호 안내"
                text =
                    """
                    안녕하세요. Dev-Station 입니다.

                    비밀번호 재설정 요청에 따라 임시 비밀번호를 발급해드렸습니다.
                    아래의 비밀번호로 로그인 후, 반드시 새 비밀번호로 변경해주세요.

                    임시 비밀번호: $newPassword
                    """.trimIndent()
            }

            mailSender.send(message)
            log.info("[비밀번호 재설정] 임시 비밀번호 전송 완료: {}", email)

        } catch (e: Exception) {
            log.error("임시 비밀번호 전송 실패: {}", e.message, e)
            throw ErrorException(ErrorCode.EMAIL_SEND_FAILED)
        }
    }
}
