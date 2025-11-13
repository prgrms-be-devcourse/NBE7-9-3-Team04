package com.backend.api.user.controller

import com.backend.api.user.dto.request.UserLoginRequest
import com.backend.api.user.dto.request.UserSignupRequest
import com.backend.api.user.dto.response.UserLoginResponse
import com.backend.api.user.dto.response.UserLoginResponse.Companion.from
import com.backend.api.user.dto.response.UserSignupResponse
import com.backend.api.user.service.EmailService
import com.backend.api.user.service.UserService
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import com.backend.global.security.JwtTokenProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "사용자 관련 API")
class UserController(
    private val userService: UserService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val rq: Rq,
    private val emailService: EmailService
) {

    @PostMapping("/login")
    @Operation(summary = "사용자 로그인")
    fun login(@RequestBody request: UserLoginRequest): ApiResponse<UserLoginResponse> {
        val response = userService.login(request)

        rq.setCookie("accessToken", response.accessToken, (jwtTokenProvider.getAccessTokenExpireTime()).toInt())
        rq.setCookie("refreshToken", response.refreshToken, (jwtTokenProvider.getRefreshTokenExpireTime()).toInt())

        return ApiResponse.ok(
            "로그인을 성공했습니다.",
            response
        )
    }

    @DeleteMapping("/logout")
    @Operation(summary = "사용자 로그아웃")
    fun logout(): ApiResponse<Void> {
        val user = rq.getUser()
        userService.logout(user.id)

        rq.deleteCookie("accessToken")
        rq.deleteCookie("refreshToken")

        return ApiResponse.ok("로그아웃이 되었습니다.", null)
    }

    @PostMapping("/signup")
    @Operation(summary = "사용자 회원가입")
    fun signup(@RequestBody request: UserSignupRequest): ApiResponse<UserSignupResponse> {
        val response = userService.signUp(request)

        return ApiResponse.ok(
            "회원가입이 완료되었습니다.",
            response
        )
    }

    @PostMapping("/sendEmail")
    @Operation(summary = "이메일 인증 코드 전송", description = "회원가입 시 이메일 인증 코드를 전송합니다.")
    fun sendEmailVerificationCode(@RequestParam email: String): ApiResponse<Void> {
        userService.sendEmailVerification(email)
        return ApiResponse.ok("이메일 인증 코드가 전송되었습니다.", null)
    }

    @PostMapping("/verifyCode")
    @Operation(summary = "이메일 인증 코드 검증", description = "회원가입 시 이메일 인증 코드를 검증합니다.")
    fun verifyEmailCode(@RequestParam email: String, @RequestParam code: String): ApiResponse<Void> {
        userService.verifyEmailCode(email, code)
        return ApiResponse.ok("이메일 인증 코드가 검증되었습니다.", null)
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급")
    fun refresh(): ApiResponse<Void> {
        val refreshToken = rq.getCookieValue("refreshToken")
            ?: throw ErrorException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)

        val newTokens = userService.createAccessTokenFromRefresh(refreshToken)

        rq.setCookie("accessToken", newTokens.accessToken, (jwtTokenProvider.getAccessTokenExpireTime()).toInt())
        rq.setCookie("refreshToken", newTokens.refreshToken, (jwtTokenProvider.getRefreshTokenExpireTime()).toInt())

        return ApiResponse.ok("새로운 토큰이 발급되었습니다.", null)
    }

    @get:Operation(summary = "현재 로그인된 사용자 정보")
    @get:GetMapping("/check")
    val currentUser: ApiResponse<UserLoginResponse>
        get() {
            val user = rq.getUser()
            return ApiResponse.ok(
                "현재 로그인된 사용자 정보입니다.",
                from(user)
            )
        }

    @PostMapping("/findId")
    @Operation(summary = "아이디 찾기", description = "사용자 아이디를 찾습니다.")
    fun findUserId(@RequestParam name: String, @RequestParam email: String): ApiResponse<String> {
        val userId = userService.findUserIdByNameAndEmail(name, email)
        return ApiResponse.ok("아이디를 찾았습니다.", userId)
    }

    @PostMapping("/findPassword")
    @Operation(summary = "비밀번호 찾기", description = "비밀번호를 재설정합니다.")
    fun findPassword(
        @RequestParam userId: String,
        @RequestParam name: String,
        @RequestParam email: String
    ): ApiResponse<Void> {
        val isValid = userService.verifyUserInfo(userId, name, email)

        if (!isValid) {
            throw ErrorException(ErrorCode.NOT_FOUND_USER)
        }

        val newPassword = RandomStringUtils.randomAlphanumeric(10)
        userService.updatePassword(userId, newPassword)
        emailService.sendNewPassword(email, newPassword)

        return ApiResponse.ok("새 비밀번호가 이메일로 전송되었습니다.", null)
    }
}
