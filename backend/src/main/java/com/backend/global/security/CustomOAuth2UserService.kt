package com.backend.global.security

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class CustomOAuth2UserService(
    private val restTemplate: RestTemplate
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)
        val attributes = HashMap(oAuth2User.attributes) // 기존 attributes 복사

        val accessToken = userRequest.accessToken.tokenValue

        // GitHub 이메일 가져오기
        val emailResponse = restTemplate.exchange(
            "https://api.github.com/user/emails",
            HttpMethod.GET,
            HttpEntity<Any>(HttpHeaders().apply { setBearerAuth(accessToken) }),
            Array<GitHubEmail>::class.java
        ).body

        val primaryEmail = emailResponse?.firstOrNull { it.primary && it.verified }?.email

        if (primaryEmail != null) {
            attributes["email"] = primaryEmail
        }

        return DefaultOAuth2User(
            oAuth2User.authorities,
            attributes,
            "login" // 기본 username key
        )
    }
}

data class GitHubEmail(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
    val visibility: String?
)
