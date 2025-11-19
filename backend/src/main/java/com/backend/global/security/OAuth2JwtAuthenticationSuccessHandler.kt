package com.backend.global.security

import com.backend.api.user.service.RefreshRedisService
import com.backend.api.user.service.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2JwtAuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val refreshRedisService: RefreshRedisService
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as DefaultOAuth2User
        val attributes = oAuth2User.attributes
        val oauthId = attributes["id"].toString()
        // 1. 기존 회원 조회
        val user = userService.findUserByOauthId(oauthId)

        if (user != null) {
            val email = attributes["email"] as String? ?: ""

            // 기존 회원이면 JWT 발급
            val accessToken = jwtTokenProvider.generateAccessToken(user.id, user.email, user.role)
            val refreshToken = jwtTokenProvider.generateRefreshToken(user.id, user.email, user.role)

            // Refresh Token Redis 저장
            refreshRedisService.saveRefreshToken(
                user.id,
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpireTime()
            )

            // 쿠키에 JWT 저장
            response.addCookie(
                createCookie(
                    "accessToken",
                    accessToken,
                    jwtTokenProvider.getAccessTokenExpireTime()
                )
            )
            response.addCookie(
                createCookie(
                    "refreshToken",
                    refreshToken,
                    jwtTokenProvider.getRefreshTokenExpireTime()
                )
            )

            // 프론트로 리다이렉트
            response.sendRedirect("http://localhost:3000/auth/oauth?token=$accessToken&email=$email&oauthId=$oauthId")
        } else {
            // 신규 회원이면 프론트 회원가입 페이지로 리다이렉트
            val email = attributes["email"] as String? ?: ""
            val name = attributes["login"] as String? ?: ""
            val nickname = attributes["name"] as String? ?: ""
            val githubUrl = "https://github.com/${attributes["login"] as String? ?: ""}"
            val avatarUrl = attributes["avatar_url"] as String?

            response.sendRedirect(
                "http://localhost:3000/auth/oauth/signup" +
                        "?email=$email" +
                        "&name=$name" +
                        "&nickname=$nickname" +
                        "&githubUrl=$githubUrl" +
                        "&avatarUrl=$avatarUrl" +
                        "&oauthId=$oauthId"
            )
        }
    }

    private fun createCookie(name: String, value: String, maxAge: Long) =
        Cookie(name, value).apply {
            path = "/"
            isHttpOnly = true
            this.maxAge = maxAge.toInt()
        }
}
