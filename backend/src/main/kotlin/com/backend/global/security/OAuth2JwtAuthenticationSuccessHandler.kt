package com.backend.global.security

import com.backend.api.user.service.UserService
import com.backend.global.Rq.Rq
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
    private val rq: Rq
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as DefaultOAuth2User
        val attributes = oAuth2User.attributes
        val oauthId = attributes["id"].toString()

        val user = userService.findUserByOauthId(oauthId)
        if (user != null) {
            // 기존 회원이면 로그인
            val loginResponse = userService.login(oauthId = oauthId)
            val email = loginResponse.email

            rq.setCookie("accessToken", loginResponse.accessToken, (jwtTokenProvider.getAccessTokenExpireTime()).toInt())
            rq.setCookie("refreshToken", loginResponse.refreshToken, (jwtTokenProvider.getRefreshTokenExpireTime()).toInt())
            // 프론트로 리다이렉트
            response.sendRedirect("http://localhost:3000/auth/oauth?email=$email&oauthId=$oauthId")
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
}
