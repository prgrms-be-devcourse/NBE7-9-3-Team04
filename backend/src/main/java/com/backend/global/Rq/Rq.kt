package com.backend.global.Rq

import com.backend.domain.user.entity.User
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import com.backend.global.security.CustomUserDetails
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class Rq(
    private val request: HttpServletRequest,
    private val response: HttpServletResponse
) {

    fun getUser(): User
        {
            val authentication: Authentication =
                SecurityContextHolder.getContext().authentication
                    ?: throw ErrorException(ErrorCode.UNAUTHORIZED_USER)

            val details = authentication.principal as? CustomUserDetails
                ?: throw ErrorException(ErrorCode.UNAUTHORIZED_USER)

            return details.user
        }

    fun setCookie(name: String, value: String?, maxAge: Int) {

        val value = value ?: ""

        val cookie = Cookie(name, value).apply{
            path = "/"
            isHttpOnly = true
            domain = "localhost"
            secure = true
            this.maxAge = if(value.isBlank()) 0 else maxAge
            setAttribute("SameSite", "None")
        }

        response.addCookie(cookie)
    }

    fun deleteCookie(name: String) {
        setCookie(name, "", 0)
    }

    fun getCookieValue(name: String): String? {
        if (request.getCookies() == null) return null

        val cookies = request.cookies ?: return null

        return cookies
            .firstOrNull {it.name == name}
            ?.value
    }
}