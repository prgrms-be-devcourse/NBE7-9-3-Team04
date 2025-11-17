package com.backend.api.review.controller

import com.backend.api.global.JwtTest
import com.backend.api.question.service.AiQuestionService
import com.backend.api.review.dto.request.AiReviewbackRequest
import com.backend.domain.resume.entity.Resume
import com.backend.domain.resume.repository.ResumeRepository
import com.backend.domain.review.entity.Review
import com.backend.domain.review.repository.ReviewRepository
import com.backend.domain.subscription.entity.Subscription
import com.backend.domain.subscription.entity.SubscriptionType
import com.backend.domain.subscription.repository.SubscriptionRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AiReviewControllerTest(
    private val mockMvc: MockMvc,
    private val reviewRepository: ReviewRepository,
    private val resumeRepository: ResumeRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val userRepository: UserRepository
) : JwtTest() {

    @MockBean
    private lateinit var aiQuestionService: AiQuestionService

    @MockBean
    private lateinit var rq: Rq

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(
            User.builder()
                .email("premium@test.com")
                .name("테스트 유저")
                .nickname("tester")
                .password("1234")
                .image("default.png")
                .github("https://github.com/tester")
                .role(Role.USER)
                .build()
        )

        Mockito.`when`(rq.getUser()).thenReturn(testUser)
    }

    private fun createPremiumSubscription(user: User): Subscription {
        var subscription = Subscription.builder()
            .user(user)
            .subscriptionType(SubscriptionType.BASIC)
            .isActive(false)
            .subscriptionName("BASIC")
            .price(0L)
            .questionLimit(5)
            .startDate(LocalDateTime.now())
            .build()

        subscription.activatePremium("test-billing-key-123")


        subscription = subscriptionRepository.save(subscription)
        user.subscription = subscription
        userRepository.saveAndFlush(user)

        return subscription
    }

    private fun createAndSaveReview(content: String): Review {
        return reviewRepository.save(
            Review.builder()
                .AiReviewContent(content)
                .user(testUser)
                .build()
        )
    }

    @Nested
    @DisplayName("포트폴리오 AI 첨삭 생성 API")
    inner class CreateAiReviewTest {
        @Test
        @DisplayName("성공 - 프리미엄 등급의 사용자가 AI 첨삭을 생성합니다.")
        fun createAiReview_Success() {
            // given
            createPremiumSubscription(testUser)

            val resume = resumeRepository.save(
                Resume.builder()
                    .user(testUser)
                    .content("테스트 이력서 내용입니다.")
                    .build()
            )


            Mockito.`when`(aiQuestionService.getAiReviewContent(Mockito.any(AiReviewbackRequest::class.java)))
                .thenReturn("AI가 생성한 첨삭 내용입니다.")

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/portfolio-review")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("CREATED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("포트폴리오 AI 첨삭이 완료되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.reviewId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.feedbackContent").exists())
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 로그인하지 않은 사용자는 AI 첨삭을 생성할 수 없습니다.")
        fun createAiReview_Fail_Unauthorized() {
            // given
            SecurityContextHolder.clearContext()
            Mockito.`when`(rq.getUser()).thenThrow(ErrorException(ErrorCode.UNAUTHORIZED_USER))
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/portfolio-review")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("로그인된 사용자가 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 일반 등급의 사용자는 AI 첨삭을 생성할 수 없습니다.")
        fun createAiReview_Fail_NotPremium() {
            // given
            val basic = subscriptionRepository.save(
                Subscription.builder()
                    .user(testUser)
                    .subscriptionType(SubscriptionType.BASIC)
                    .isActive(false)
                    .subscriptionName("BASIC")
                    .price(0L)
                    .questionLimit(5)
                    .startDate(LocalDateTime.now())
                    .build()
            )
            testUser.subscription = basic
            userRepository.saveAndFlush(testUser)

            resumeRepository.save(
                Resume.builder()
                    .user(testUser)
                    .content("테스트 이력서 내용입니다.")
                    .build()
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/portfolio-review")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("포트폴리오 첨삭은 PREMIUM 등급 사용자만 이용 가능합니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("AI 첨삭 단건 조회 API")
    inner class GetReviewByIdTest {
        @Test
        @DisplayName("성공 - 자신의 AI 첨삭을 조회합니다.")
        fun getReviewById_Success() {
            // given
            createPremiumSubscription(testUser)
            val savedReview = createAndSaveReview("AI 첨삭 내용입니다.")
            val reviewId = savedReview.id

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/portfolio-review/{reviewId}", reviewId)
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("AI 첨삭 조회가 완료되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.reviewId").value(reviewId))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("AI 첨삭 다건 조회 API")
    inner class GetMyReviewsTest {
        @Test
        @DisplayName("성공 - 로그인한 사용자의 모든 AI 첨삭 목록을 조회합니다.")
        fun getMyReviews_Success() {
            // given
            createPremiumSubscription(testUser)

            createAndSaveReview("첫 번째 첨삭")
            Thread.sleep(10)
            val latestReview = createAndSaveReview("두 번째 첨삭")

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/portfolio-review/reviews")
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("내 AI 첨삭 목록 조회가 완료되었습니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].reviewId").value(latestReview.id)) // 4. '.getId()' -> '.id'
                .andDo(MockMvcResultHandlers.print())
        }
    }
}