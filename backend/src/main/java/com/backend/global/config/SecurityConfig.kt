package com.backend.global.config

import com.backend.global.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http

            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .headers { headers ->
                headers.addHeaderWriter(
                    XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)
                )
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/favicon.ico").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/webjars/**"
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/posts/{postId}/comments",
                        "/api/v1/posts",
                        "/api/v1/posts/category/*",
                        "/api/v1/posts/{postId}",
                        "/api/v1/payments/*",
                        "/api/v1/qna",
                        "/api/v1/qna/**",
                        "/api/v1/questions",
                        "/api/v1/questions/**",
                        "/api/v1/questions/{questionId}/answers",
                        "/api/v1/questions/{questionId}/answers/*"
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/users/login",
                        "/api/v1/users/signup",
                        "/api/v1/users/refresh",
                        "/api/v1/users/sendEmail",
                        "/api/v1/users/verifyCode"
                    ).permitAll()
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .exceptionHandling { handling ->
                // 2. AuthenticationEntryPoint SAM 변환을 간결한 람다로 처리
                handling.authenticationEntryPoint { _, response, _ ->
                    response.contentType = "application/json; charset=UTF-8"
                    response.status = 401
                    response.writer.write(
                        """
                        {
                            "status": "UNAUTHORIZED",
                            "message": "로그인 후 이용해주세요.",
                            "data" : null
                        }
                        """.trimIndent()
                    )
                }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        // 3. 'apply'와 프로퍼티 접근, 불변 'listOf'로 변경
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000") // 불변, non-null List
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true // 프로퍼티 접근
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/api/**", configuration)
        return source
    }
}