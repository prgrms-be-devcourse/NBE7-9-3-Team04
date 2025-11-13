package com.backend.global.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    private val excludeUri = listOf(
        "/api/v1/users/signup",
        "/api/v1/users/login",
        "/api/v1/users/sendEmail",
        "/api/v1/users/verifyCode",
        "/api/v1/users/refresh"
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        //회원가입과 로그인은 인증 x
        if (request.requestURI in excludeUri) {
            filterChain.doFilter(request, response)
            return
        }

        val token = resolveToken(request)

        if (token != null && jwtTokenProvider.validateToken(token)) {
            val authentication = jwtTokenProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {

        //헤더에서 우선적으로 찾기
        val bearer = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7)
        }

        val cookies = request.cookies ?: return null

        //쿠키도 확인
        return cookies
            .firstOrNull { it.name == "accessToken" }
            ?.value
    }
}
