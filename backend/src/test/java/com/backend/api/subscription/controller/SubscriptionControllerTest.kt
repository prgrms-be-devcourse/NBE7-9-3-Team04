package com.backend.api.subscription.controller

import com.backend.domain.subscription.entity.Subscription
import com.backend.domain.subscription.entity.SubscriptionType
import com.backend.domain.subscription.repository.SubscriptionRepository
import com.backend.domain.user.entity.AccountStatus
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
class SubscriptionControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var subscriptionRepository: SubscriptionRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var rq: Rq

    private lateinit var testUser1: User
    private lateinit var testUser2: User
    private lateinit var activeSubscription: Subscription
    private lateinit var deactiveSubscription: Subscription

    @BeforeEach
    fun setUp() {

        testUser1 = User(
            email = "testuser1@test.com",
            password = "user1234!",
            name = "유저1",
            nickname = "user1",
            age = 25,
            github = "github.com/user1",
            image = null,
            role = Role.USER,
            accountStatus = AccountStatus.ACTIVE,
            aiQuestionUsedCount = 0,
            subscription = null
        )
        userRepository.save(testUser1)


        activeSubscription = Subscription(
            subscriptionType = SubscriptionType.PREMIUM,
            active = true,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusMonths(1),
            nextBillingDate = LocalDate.now().plusMonths(1),
            questionLimit = 8,
            subscriptionName = "PREMIUM",
            price = 9900L,
            billingKey = "billingKey123",
            customerKey = "customerKey123",
            user = testUser1
        )
        subscriptionRepository.save(activeSubscription)

        testUser2 = User(
            email = "testuser2@test.com",
            password = "user1234!",
            name = "유저2",
            nickname = "user2",
            age = 25,
            github = "github.com/user2",
            image = null,
            role = Role.USER,
            accountStatus = AccountStatus.ACTIVE,
            aiQuestionUsedCount = 0,
            subscription = null
        )
        userRepository.save(testUser2)

        deactiveSubscription = Subscription(
            subscriptionType = SubscriptionType.BASIC,
            active = false,
            startDate = LocalDateTime.now().minusMonths(1),
            endDate = LocalDateTime.now(),
            nextBillingDate = null,
            questionLimit = 5,
            subscriptionName = "BASIC",
            price = 0L,
            billingKey = null,
            customerKey = "customerKey1234",
            user = testUser2
        )
        subscriptionRepository.save(deactiveSubscription)
    }

    @Nested
    @DisplayName("구독 조회 API")
    inner class T1 {
        @Test
        @DisplayName("정상 작동")
        fun success() {
            //given

            Mockito.`when`(rq.getUser()).thenReturn(testUser1)

            //when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/subscriptions/me")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)

            )

            //then
            resultActions
                .andExpect(handler().handlerType(SubscriptionController::class.java))
                .andExpect(handler().methodName("getMySubscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("구독 정보를 불러왔습니다."))
                .andExpect(jsonPath("$.data.userId").value(testUser1.id))
                .andExpect(jsonPath("$.data.subscriptionName").value("PREMIUM"))
                .andExpect(jsonPath("$.data.subscriptionType").value("PREMIUM"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.price").value(9900))
                .andExpect(jsonPath("$.data.customerKey").value("customerKey123"))
                .andExpect(jsonPath("$.data.billingKey").value("billingKey123"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("구독 조회 실패 = 유저의 구독이 존재하지 않을 때")
        @Throws(Exception::class)
        fun fail() {
            //given

            val newUser = User(
                email = "newuser@test.com",
                password = "user1234!",
                name = "새유저",
                nickname = "새유저",
                age = 25,
                github = "github.com/newUser",
                image = null,
                role = Role.USER,
                accountStatus = AccountStatus.ACTIVE,
                aiQuestionUsedCount = 0,
                subscription = null
            )
            userRepository.save(newUser)

            Mockito.`when`(rq.getUser()).thenReturn(newUser)


            //when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/subscriptions/me")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)

            )

            //then
            resultActions
                .andExpect(handler().handlerType(SubscriptionController::class.java))
                .andExpect(handler().methodName("getMySubscription"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 고객의 구독 정보를 찾을 수 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }


    @Nested
    @DisplayName("구독 취소 API")
    inner class T2 {
        @Test
        @DisplayName("정상 작동")
        fun success() {
            //given

            Mockito.`when`<User>(rq.getUser()).thenReturn(testUser1)


            //when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete(
                    "/api/v1/subscriptions/cancel/{customerKey}",
                    activeSubscription.customerKey
                )
                    .accept(MediaType.APPLICATION_JSON)

            )

            //then
            resultActions
                .andExpect(handler().handlerType(SubscriptionController::class.java))
                .andExpect(handler().methodName("cancelSubscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("구독이 성공적으로 취소되었습니다."))
                .andExpect(jsonPath("$.data.customerKey").value(activeSubscription.customerKey))
                .andDo(MockMvcResultHandlers.print())

            val updated= subscriptionRepository.findByCustomerKey(activeSubscription.customerKey!!)

            assert(updated?.billingKey == null)
        }

        @Test
        @DisplayName("구독 취소 실패 - 이미 비활성화된 구독")
        fun fail1() {
            //given

            Mockito.`when`(rq.getUser()).thenReturn(testUser2)


            //when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete(
                    "/api/v1/subscriptions/cancel/{customerKey}",
                    deactiveSubscription.customerKey
                )
                    .accept(MediaType.APPLICATION_JSON)

            )

            //then
            resultActions
                .andExpect(handler().handlerType(SubscriptionController::class.java))
                .andExpect(handler().methodName("cancelSubscription"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("비활성화된 구독입니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("구독 취소 실패 - 존재하지 않는 customerKey")
        fun fail2() {
            //given

            Mockito.`when`(rq.getUser()).thenReturn(testUser1)


            //when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/subscriptions/cancel/{customerKey}", "invalid_key")
                    .accept(MediaType.APPLICATION_JSON)

            )

            //then
            resultActions
                .andExpect(handler().handlerType(SubscriptionController::class.java))
                .andExpect(handler().methodName("cancelSubscription"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 고객의 구독 정보를 찾을 수 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }
}
