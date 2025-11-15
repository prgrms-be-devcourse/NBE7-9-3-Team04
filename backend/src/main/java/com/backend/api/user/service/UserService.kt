package com.backend.api.user.service

import com.backend.api.user.dto.request.UserLoginRequest
import com.backend.api.user.dto.request.UserSignupRequest
import com.backend.api.user.dto.response.TokenResponse
import com.backend.api.user.dto.response.UserLoginResponse
import com.backend.api.user.dto.response.UserSignupResponse
import com.backend.api.user.event.publisher.UserSignupEvent
import com.backend.domain.ranking.entity.Ranking
import com.backend.domain.ranking.entity.Tier
import com.backend.domain.ranking.repository.RankingRepository
import com.backend.domain.subscription.entity.Subscription
import com.backend.domain.subscription.entity.SubscriptionType
import com.backend.domain.subscription.repository.SubscriptionRepository
import com.backend.domain.user.entity.AccountStatus
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.entity.search.UserDocument
import com.backend.domain.user.repository.UserRepository
import com.backend.domain.user.repository.VerificationCodeRepository
import com.backend.domain.user.repository.search.UserSearchRepository
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import com.backend.global.security.JwtTokenProvider
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val subscriptionRepository: SubscriptionRepository,
    private val emailService: EmailService,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val rankingRepository: RankingRepository,
    private val userSearchRepository: UserSearchRepository,
    private val refreshRedisService: RefreshRedisService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun signUp(request: UserSignupRequest): UserSignupResponse {
        //이메일 중복 검사

        if (userRepository.findByEmail(request.email) != null) {
            throw ErrorException(ErrorCode.DUPLICATE_EMAIL)
        }

        // 이메일 인증 여부 확인
        if (!emailService.isVerified(request.email)) {
            throw ErrorException(ErrorCode.EMAIL_NOT_VERIFIED)
        }

        // 사용자 생성
        val encodedPassword = passwordEncoder.encode(request.password)
        val user= User(
            email = request.email,
            password = encodedPassword,
            name = request.name,
            nickname = request.nickname,
            age = request.age,
            github = request.github,
            image = request.image,
            role = Role.USER
        )

        verificationCodeRepository.findByEmail(request.email)?.let {
            verificationCodeRepository.delete(it)
        }

        userRepository.save(user)

        //TODO UserDocument builder 제거 필요
        userSearchRepository.save(
            UserDocument.builder()
                .id(user.id.toString())
                .name(user.name)
                .nickname(user.nickname)
                .email(user.email)
                .build()
        )
        val basicSubscription = Subscription(
            user = user,
            subscriptionType = SubscriptionType.BASIC,
            subscriptionName = "BASIC",
            active = false,
            price = 0L,
            questionLimit = 5, // 무료 사용자는 질문 제한 5회
            startDate = LocalDateTime.now(),
            endDate = null, // BASIC은 실질적 만료 개념 X
            nextBillingDate = null,
            billingKey = null,
            customerKey = UUID.randomUUID().toString() // Toss에서 사용할 유저별 key
        )

        subscriptionRepository.save(basicSubscription)

        val ranking = Ranking(
            user = user,
            totalScore = 0,
            tier = Tier.UNRATED,
            rankValue = 0
        )

        user.assignSubscription(basicSubscription)
        rankingRepository.save(ranking)


        eventPublisher.publishEvent(UserSignupEvent(user))
        return UserSignupResponse.from(user, ranking)
    }

    @Transactional
    fun login(request: UserLoginRequest): UserLoginResponse {
        val user: User = userRepository.findByEmail(request.email)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_EMAIL)

        if (!user.validateLoginAvaliable()) {

            when(user.accountStatus){
                AccountStatus.BANNED -> throw ErrorException(ErrorCode.ACCOUNT_BANNED)
                AccountStatus.DEACTIVATED -> throw ErrorException(ErrorCode.ACCOUNT_DEACTIVATED)
                else -> throw ErrorException(ErrorCode.UNAUTHORIZED_USER)
            }
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ErrorException(ErrorCode.WRONG_PASSWORD)
        }

        val accessToken = jwtTokenProvider.generateAccessToken(user.id, user.email, user.role)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id, user.email, user.role)

        refreshRedisService.saveRefreshToken(
            user.id,
            refreshToken,
            jwtTokenProvider.getRefreshTokenExpireTime()
        )

        return UserLoginResponse.from(user, accessToken, refreshToken)
    }

    @Transactional
    fun logout(userId: Long) {
        refreshRedisService.deleteRefreshToken(userId)
    }

    @Transactional(readOnly = true)
    fun getUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow{ ErrorException(ErrorCode.NOT_FOUND_USER) }
    }

    @Transactional
    fun createAccessTokenFromRefresh(requestRefreshToken: String): TokenResponse {
        //클라이언트 요청으로부터 refreshToken 유효성 검사

        if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw ErrorException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        //요청된 refreshToken으로부터 id 추출
        val userId = jwtTokenProvider.getIdFromToken(requestRefreshToken)
            ?: throw ErrorException(ErrorCode.INVALID_REFRESH_TOKEN)

        //redis에 저장된 refreshToken 조회
        val savedRefreshToken = refreshRedisService.getRefreshToken(userId)
            ?: throw ErrorException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)

        //요청과 redis 동일한지 비교
        if (savedRefreshToken != requestRefreshToken) {
            throw ErrorException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val user = userRepository.findById(userId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_USER) }


        //새로운 토큰 발급
        val newAccessToken = jwtTokenProvider.generateAccessToken(user.id, user.email, user.role)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(user.id, user.email, user.role)

        refreshRedisService.saveRefreshToken(
            user.id,
            newRefreshToken,
            jwtTokenProvider.getRefreshTokenExpireTime()
        )

        return TokenResponse(newAccessToken, newRefreshToken)
    }

    @Transactional
    fun sendEmailVerification(email: String) {
        emailService.createAndSendVerificationCode(email)
    }

    // 🟩 이메일 인증 코드 검증
    @Transactional
    fun verifyEmailCode(email: String, code: String) {
        emailService.verifyCode(email, code)
    }

    @Transactional(readOnly = true)
    fun findUserIdByNameAndEmail(name: String, email: String): String {
        val user: User = userRepository.findByEmail(email)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_USER)

        if (user.name != name) throw ErrorException(ErrorCode.NOT_FOUND_USER)

        return user.email
    }

    @Transactional(readOnly = true)
    fun verifyUserInfo(userId: String, name: String, email: String): Boolean {
        return userRepository.findByEmail(userId)
            ?.let { it.name == name && it.email == email } ?: false
    }

    @Transactional
    fun updatePassword(userId: String, newPassword: String) {
        val user: User = userRepository.findByEmail(userId)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_USER)

        val encodedPassword = passwordEncoder.encode(newPassword)
        user.changePassword(encodedPassword)
        userRepository.save(user)
    }
}
