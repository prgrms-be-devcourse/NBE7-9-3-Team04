package com.backend.api.review.controller;

import com.backend.api.global.JwtTest;
import com.backend.api.question.service.AiQuestionService;
import com.backend.domain.resume.entity.Resume;
import com.backend.domain.resume.repository.ResumeRepository;
import com.backend.domain.review.entity.Review;
import com.backend.domain.review.repository.ReviewRepository;
import com.backend.domain.subscription.entity.Subscription;
import com.backend.domain.subscription.entity.SubscriptionType;
import com.backend.domain.subscription.repository.SubscriptionRepository;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;
import com.backend.global.Rq.Rq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
class AiReviewControllerTest extends JwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @MockBean
    private AiQuestionService aiQuestionService;

    @MockBean
    private Rq rq;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 1️⃣ 기본 유저 저장
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
        );

        // 2️⃣ rq.getUser() mock 설정
        Mockito.when(rq.getUser()).thenReturn(testUser);
    }

    private Subscription createPremiumSubscription(User user) {
        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionType(SubscriptionType.BASIC)
                .isActive(false)
                .subscriptionName("BASIC")
                .price(0L)
                .questionLimit(5)
                .startDate(LocalDateTime.now())
                .build();

        // 프리미엄 전환
        subscription.activatePremium("test-billing-key-123");

        // 관계 설정 및 저장
        subscription = subscriptionRepository.save(subscription);
        user.setSubscription(subscription);
        userRepository.saveAndFlush(user);

        return subscription;
    }

    private Review createAndSaveReview(String content) {
        return reviewRepository.save(
                Review.builder()
                        .AiReviewContent(content)
                        .user(testUser)
                        .build()
        );
    }

    @Nested
    @DisplayName("포트폴리오 AI 첨삭 생성 API")
    class CreateAiReviewTest {

        @Test
        @DisplayName("성공 - 프리미엄 등급의 사용자가 AI 첨삭을 생성합니다.")
        void createAiReview_Success() throws Exception {
            // given
            createPremiumSubscription(testUser);

            Resume resume = resumeRepository.save(
                    Resume.builder()
                            .user(testUser)
                            .content("테스트 이력서 내용입니다.")
                            .build()
            );

            given(aiQuestionService.getAiReviewContent(any()))
                    .willReturn("AI가 생성한 첨삭 내용입니다.");

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/portfolio-review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$.message").value("포트폴리오 AI 첨삭이 완료되었습니다."))
                    .andExpect(jsonPath("$.data.reviewId").exists())
                    .andExpect(jsonPath("$.data.feedbackContent").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 로그인하지 않은 사용자는 AI 첨삭을 생성할 수 없습니다.")
        void createAiReview_Fail_Unauthorized() throws Exception {
            // given
            SecurityContextHolder.clearContext();
            Mockito.when(rq.getUser()).thenReturn(null);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/portfolio-review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("로그인된 사용자가 없습니다."))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패 - 일반 등급의 사용자는 AI 첨삭을 생성할 수 없습니다.")
        void createAiReview_Fail_NotPremium() throws Exception {
            // given
            Subscription basic = subscriptionRepository.save(
                    Subscription.builder()
                            .user(testUser)
                            .subscriptionType(SubscriptionType.BASIC)
                            .isActive(false)
                            .subscriptionName("BASIC")
                            .price(0L)
                            .questionLimit(5)
                            .startDate(LocalDateTime.now())
                            .build()
            );
            testUser.setSubscription(basic);
            userRepository.saveAndFlush(testUser);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/portfolio-review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("포트폴리오 첨삭은 PREMIUM 등급 사용자만 이용 가능합니다."))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("AI 첨삭 단건 조회 API")
    class GetReviewByIdTest {

        @Test
        @DisplayName("성공 - 자신의 AI 첨삭을 조회합니다.")
        void getReviewById_Success() throws Exception {
            // given
            createPremiumSubscription(testUser);
            Review savedReview = createAndSaveReview("AI 첨삭 내용입니다.");
            Long reviewId = savedReview.getId();

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/portfolio-review/{reviewId}", reviewId)
                            .accept(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("AI 첨삭 조회가 완료되었습니다."))
                    .andExpect(jsonPath("$.data.reviewId").value(reviewId))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("AI 첨삭 다건 조회 API")
    class GetMyReviewsTest {

        @Test
        @DisplayName("성공 - 로그인한 사용자의 모든 AI 첨삭 목록을 조회합니다.")
        void getMyReviews_Success() throws Exception {
            // given
            createPremiumSubscription(testUser);

            createAndSaveReview("첫 번째 첨삭");
            Thread.sleep(10);
            Review latestReview = createAndSaveReview("두 번째 첨삭");

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/portfolio-review/reviews")
                            .accept(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("내 AI 첨삭 목록 조회가 완료되었습니다."))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].reviewId").value(latestReview.getId()))
                    .andDo(print());
        }
    }
}
