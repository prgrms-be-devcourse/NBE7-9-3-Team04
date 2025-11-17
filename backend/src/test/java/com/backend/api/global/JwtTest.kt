package com.backend.api.global

import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.entity.User.Companion.builder
import com.backend.domain.user.repository.UserRepository
import com.backend.global.security.CustomUserDetails
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class JwtTest {
    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var passwordEncoder: PasswordEncoder

    protected lateinit var mockUser: User

    @BeforeEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun setJWT() {
        val user = builder()
            .email("test@naver.com")
            .age(27)
            .github("https://github.com/test")
            .name("test")
            .password(passwordEncoder.encode("test1234"))
            .image(null)
            .role(Role.USER)
            .nickname("testnick")
            .build()


        mockUser = userRepository.save(user)

        val userDetails = CustomUserDetails(mockUser)

        val auth: Authentication = UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.authorities
        )
        SecurityContextHolder.getContext().setAuthentication(auth)
    }
}
