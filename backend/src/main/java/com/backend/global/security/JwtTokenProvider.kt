package com.backend.global.security

import com.backend.domain.user.entity.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

//JWT 핵심 유틸 클래스
@Component
class JwtTokenProvider(
    private val customUserDetailsService: CustomUserDetailsService,

    @Value("\${custom.jwt.secretPattern}")
    private val secretPattern: String,

    @Value("\${custom.jwt.access-token.expire-time}")
    private val ACCESS_TOKEN_EXPIRE_TIME: Long,

    @Value("\${custom.jwt.refresh-token.expire-time}")
    private val REFRESH_TOKEN_EXPIRE_TIME: Long
) {

    private var key: SecretKey? = null

    @PostConstruct
    fun init() {
        key = Keys.hmacShaKeyFor(secretPattern.toByteArray(StandardCharsets.UTF_8))
    }


    //토큰 공통 생성 로직
    fun generateToken(id: Long, email: String, role: Role, expireTime: Long): String {
        val now = Date()
        val expiryDate = Date(now.getTime() + expireTime) //만료시간 설정

        return Jwts.builder()
            .subject(email) // Subject에 email 저장
            .issuedAt(now)
            .expiration(expiryDate)
            .claim("userId", id) // ID는 별도 클레임으로 저장. 만약 수정 필요하면 수정 예정
            .claim("role", role.name)
            .signWith(key)
            .compact()
    }

    //AccessToken 생성 (Role 포함)
    fun generateAccessToken(id: Long, email: String, role: Role): String =
        generateToken(id, email, role, ACCESS_TOKEN_EXPIRE_TIME)


    //RefreshToken 생성 (Role 포함)
    fun generateRefreshToken(id: Long, email: String, role: Role): String =
        generateToken(id, email, role, REFRESH_TOKEN_EXPIRE_TIME)


    //claims 파싱
    private fun parseClaims(token: String): Claims =
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
        } catch (e: ExpiredJwtException) {
            e.claims
        }

    fun getEmailFromToken(token: String): String = parseClaims(token).subject


    fun getIdFromToken(token: String): Long? =
        parseClaims(token)["userId"]?.toString()?.toLong()


    fun getRoleFromToken(token: String): Role =
        Role.valueOf(parseClaims(token)["role"].toString())

    //토큰을 Security 인증 객체로 변환
    fun getAuthentication(token: String): Authentication {
        val email = getEmailFromToken(token)
        val user = customUserDetailsService.loadUserByUsername(email)
        return UsernamePasswordAuthenticationToken(
            user,
            "",
            user.authorities
        )
    }

    //유효성 검사
    fun validateToken(token: String?): Boolean =
        try {
            Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parse(token)
            true
        } catch (e: Exception) {
             false
    }

    //jwt는 생성 시 밀리초, 쿠키는 만료 시 초 사용하므로 1000나눠서 넘겨준다
    fun getAccessTokenExpireTime(): Long = ACCESS_TOKEN_EXPIRE_TIME / 1000

    fun getRefreshTokenExpireTime(): Long = REFRESH_TOKEN_EXPIRE_TIME / 1000
}
