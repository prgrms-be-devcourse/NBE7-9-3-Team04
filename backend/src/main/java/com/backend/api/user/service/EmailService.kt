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

    // 인증코드 생성 + 이메일 발송
    @Transactional
    fun createAndSendVerificationCode(email: String) {
        verificationCodeRepository.findByEmail(email)
            ?.let { verificationCodeRepository.delete(it) }

        val code = generateVerificationCode()

        val verification = VerificationCode(
            email = email,
            code = code,
            expiresAt = LocalDateTime.now().plusMinutes(5),
            verified = false
        )

        verificationCodeRepository.save(verification)

        sendVerificationMailAsync(email, code)
    }

    // 인증 메일 비동기 발송
    @Async("mailExecutor")
    fun sendVerificationMailAsync(email: String, code: String) {
        try {
            val message = SimpleMailMessage().apply {
                setTo(email)
                subject = "[Dev-Station] 이메일 인증 코드"
                text =
                    """
                    안녕하세요. Dev-Station 입니다.

                    아래 인증 코드를 입력해 주세요.
                    인증코드: $code

                    본 코드는 5분간 유효합니다.
                    """.trimIndent()
            }

            mailSender.send(message)
            log.info("[이메일 인증] 인증코드 전송 완료: {}", email)
        } catch (e: Exception) {
            log.error("이메일 인증코드 전송 실패: {}", e.message, e)
            throw ErrorException(ErrorCode.EMAIL_SEND_FAILED)
        }
    }

    // 인증코드 생성
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

    // 이메일 인증 여부 조회
    fun isVerified(email: String): Boolean =
        verificationCodeRepository.findByEmail(email)?.verified ?: false

    // 회원가입 환영 이메일
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
                    다양한 기능을 이용하실 수 있습니다.

                    앞으로 좋은 서비스로 보답하겠습니다!
                    """.trimIndent()
            }

            mailSender.send(message)
            log.info("[회원가입 메일] 환영 메일 전송 완료: {}", user.email)
        } catch (e: Exception) {
            log.error("회원가입 환영 메일 전송 실패: {}", e.message, e)
        }
    }

    // 계정 상태 변경 메일
    @Async("mailExecutor")
    fun sendStatusChangeMail(user: User, penalty: UserPenalty?) {

        val status = user.accountStatus

        // 정지 / 영구정지 상태는 penalty 필수
        if ((status == AccountStatus.SUSPENDED || status == AccountStatus.BANNED) && penalty == null) {
            log.error("패널티가 있어야 하는 상태인데 penalty=null | user={}", user.email)
            return
        }

        val p = penalty // 가독성을 위해 별도 변수화

        // 이메일 제목 + 내용 처리
        val (subject, content) = when (status) {

            AccountStatus.SUSPENDED -> {
                val reason = p?.reason ?: "사유 정보 없음"
                val endAt = p?.endAt ?: "미정"

                "[Dev-Station] 계정 일시정지 안내" to """
                안녕하세요, ${user.name}님.

                회원님의 계정이 일시정지되었습니다.
                사유: $reason
                종료일: $endAt
                """.trimIndent()
            }

            AccountStatus.BANNED -> {
                val reason = p?.reason ?: "사유 정보 없음"

                "[Dev-Station] 계정 영구 정지 안내" to """
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

        // 이메일 발송
        val message = SimpleMailMessage().apply {
            setTo(user.email)
            this.subject = subject
            text = content
        }

        mailSender.send(message)
    }

    // 임시 비밀번호 발송
    fun sendNewPassword(email: String, newPassword: String) {
        try {
            val message = SimpleMailMessage().apply {
                setTo(email)
                subject = "[Dev-Station] 임시 비밀번호 안내"
                text =
                    """
                    안녕하세요. Dev-Station 입니다.

                    요청하신 임시 비밀번호를 발급해드립니다.
                    로그인 후 반드시 새 비밀번호로 변경해주세요.

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
