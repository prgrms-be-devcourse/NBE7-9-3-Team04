package com.backend.api.billing.controller

import com.backend.api.billing.dto.request.BillingRequest
import com.backend.domain.subscription.entity.Subscription
import com.backend.domain.subscription.entity.SubscriptionType
import com.backend.domain.subscription.repository.SubscriptionRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@TestInstance(
    TestInstance.Lifecycle.PER_CLASS
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BillingControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val subscriptionRepository: SubscriptionRepository
) {

    @MockBean
    lateinit var rq: Rq

    lateinit var testUser: User
    lateinit var subscription: Subscription

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(
            User(
                email = "user@test.com",
                password = "user1234!",
                name = "유저1",
                nickname = "user",
                age = 25,
                github = "github.com/user",
                role = Role.USER
            )
        )

        subscription = subscriptionRepository.save(
            Subscription(
                subscriptionType = SubscriptionType.BASIC,
                active = false,
                startDate = LocalDateTime.now(),
                endDate = null,
                nextBillingDate = null,
                questionLimit = 5,
                subscriptionName = "BASIC",
                price = 0L,
                billingKey = null,
                customerKey = "customerKey123",
                user = testUser
            )
        )
        Mockito.`when`(rq.getUser()).thenReturn(testUser)
    }

    @Nested
    @DisplayName("빌링키 발급 API")
    inner class T1 {
        @Test
        @DisplayName("빌링 키 발급 실패 - 누락된 customerKey")

        fun fail1() {
            //given
            val invalidRequest = BillingRequest(null, "authKey123")

            //when
            val resultActions = mockMvc.perform(
                post("/api/v1/billing/confirm")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))

            )

            //then
            resultActions
                .andExpect(handler().handlerType(BillingController::class.java))
                .andExpect(handler().methodName("issueBillingKey"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("CUSTOMER_KEY가 누락되었거나 유효하지 않습니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("빌링 키 발급 실패 - 누락된 authKey")
        fun fail2() {
            //given

            val invalidRequest = BillingRequest(subscription.customerKey, null)

            //when
            val resultActions = mockMvc.perform(
                post("/api/v1/billing/confirm")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))

            )

            //then
            resultActions
                .andExpect(handler().handlerType(BillingController::class.java))
                .andExpect(handler().methodName("issueBillingKey"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("AUTH_KEY가 누락되었거나 유효하지 않습니다."))
                .andDo(print())
        }
    }
}
