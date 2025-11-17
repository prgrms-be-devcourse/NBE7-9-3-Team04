package com.backend.api.ranking.controller

import com.backend.config.TestRedisConfig
import com.backend.domain.ranking.entity.Ranking
import com.backend.domain.ranking.entity.Tier
import com.backend.domain.ranking.repository.RankingRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import(TestRedisConfig::class)
class RankingControllerTest(
    private val mockMvc: MockMvc,
    private val rankingRepository: RankingRepository,
    private val userRepository: UserRepository,
    private val stringRedisTemplate: StringRedisTemplate
) {

    @MockBean
    lateinit var rq: Rq

    lateinit var testUser: User
    lateinit var testRanking: Ranking

    companion object {
        private const val REDIS_PREFIX = "ranking_"
    }

    @BeforeEach
    fun setUp() {
        stringRedisTemplate.delete(REDIS_PREFIX)

        testUser = userRepository.save(
            User(
                email = "testuser@test.com",
                password = "user1234!",
                name = "유저",
                nickname = "user",
                age = 25,
                github = "github.com/user",
                role = Role.USER
            )
        )

        testRanking = rankingRepository.save(
            Ranking(
                totalScore = 100,
                tier = Tier.UNRATED,
                rankValue = 0,
                user = testUser
            )
        )

        stringRedisTemplate.opsForZSet().add(
            REDIS_PREFIX,
            testUser.id.toString(),
            100.0
        )

        Mockito.`when`(rq.getUser()).thenReturn(testUser)
    }

    fun createDummyRankings(count: Int, startScore: Int): List<User> {
        val users = mutableListOf<User>()
        val rankings = mutableListOf<Ranking>()

        for (i in 0 until count) {
            val dummyUser = userRepository.save(
                User(
                    email = "dummy$i@test.com",
                    password = "dummy1234!",
                    name = "더미$i",
                    nickname = "dummy$i",
                    age = 30,
                    github = "github.com/dummy$i",
                    role = Role.USER
                )
            )

            // 점수를 내림차순으로 부여
            val score = startScore - i

            rankings += Ranking(
                totalScore = score,
                tier = Tier.UNRATED,
                rankValue = 0,
                user = dummyUser
            )

            // Redis에도 랭킹 데이터 삽입 (ZADD)
            stringRedisTemplate.opsForZSet().add(
                REDIS_PREFIX,
                dummyUser.id.toString(),
                score.toDouble()
            )
            users.add(dummyUser)
        }
        rankingRepository.saveAll(rankings)
        return users
    }

    @Nested
    @DisplayName("전체 랭킹 조회 API")
    inner class T1 {
        @Test
        @DisplayName("정상 작동")
        fun success1() {

            //given
            createDummyRankings(5, 500)

            //when
            val resultActions = mockMvc.perform(
                get("/api/v1/rankings")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)

            )

            //then
            resultActions
                .andExpect(handler().handlerType(RankingController::class.java))
                .andExpect(handler().methodName("getRankings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("전체 랭킹 조회를 성공했습니다."))
                .andExpect(jsonPath("$.data.myRanking.userId").value(testUser.id))
                .andExpect(jsonPath("$.data.myRanking.rankValue").value(6)) // Redis 순위 검증
                .andExpect(jsonPath("$.data.topRankings").isArray())
                .andExpect(jsonPath("$.data.topRankings.length()").value(6))
                .andDo(print())
        }

        @Test
        @DisplayName("정상 작동 - 상위 10명만 반환하는지 확인")
        fun success2() {

            //given
            createDummyRankings(14, 500)

            //when
            val resultActions = mockMvc.perform(
                get("/api/v1/rankings")
                    .accept(MediaType.APPLICATION_JSON)
            )

            //then
            resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.topRankings.length()").value(10)) // 상위 10명 검증
                .andExpect(jsonPath("$.data.topRankings[0].totalScore").value(500))
                .andExpect(jsonPath("$.data.topRankings[9].totalScore").value(491))
                .andExpect(jsonPath("$.data.myRanking.rankValue").value(15)) // 내 순위 검증
                .andDo(print())
        }

        @Test
        @DisplayName("전체 랭킹 조회 실패 - 내 랭킹 정보가 DB에 없을 때")
        fun fail() {
            //given
            rankingRepository.deleteAll()

            val newUser = userRepository.save(
                User(
                    email = "newuser@test.com",
                    password = "user1234!",
                    name = "새유저",
                    nickname = "새유저",
                    age = 25,
                    github = "github.com/newUser",
                    role = Role.USER
                )
            )


            Mockito.`when`(rq.getUser()).thenReturn(newUser)


            //when
            val resultActions = mockMvc.perform(
                get("/api/v1/rankings")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)

            )

            //then
            resultActions
                .andExpect(handler().handlerType(RankingController::class.java))
                .andExpect(handler().methodName("getRankings"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 사용자의 랭킹 정보를 찾을 수 없습니다."))
                .andDo(print())
        }

    }

    @Nested
    @DisplayName("내 랭킹 조회 API")
    inner class T3 {
        @Test
        @DisplayName("정상 작동")
        fun success() {

            //when
            val resultActions = mockMvc.perform(
                get("/api/v1/rankings/me")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)

            )

            //then
            resultActions
                .andExpect(handler().handlerType(RankingController::class.java))
                .andExpect(handler().methodName("getMyRankingOnly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("내 랭킹 조회를 성공했습니다."))
                .andExpect(jsonPath("$.data.userId").value(testUser.id))
                .andExpect(jsonPath("$.data.nickName").value("user"))
                .andExpect(jsonPath("$.data.totalScore").value(100))
                .andExpect(jsonPath("$.data.rankValue").value(1))
                .andExpect(jsonPath("$.data.currentTier").value("UNRATED"))
                .andDo(print())
        }

        @Test
        @DisplayName("내 랭킹 조회 실패 = 유저의 랭킹이 존재하지 않을 때")
        fun fail() {
            //given
            val newUser = userRepository.save(
                User(
                    email = "newuser@test.com",
                    password = "user1234!",
                    name = "새유저",
                    nickname = "새유저",
                    age = 25,
                    github = "github.com/newUser",
                    role = Role.USER
                )
            )

            Mockito.`when`(rq.getUser()).thenReturn(newUser)


            //when
            val resultActions = mockMvc.perform(
                get("/api/v1/rankings/me")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)

            )

            //then
            resultActions
                .andExpect(handler().handlerType(RankingController::class.java))
                .andExpect(handler().methodName("getMyRankingOnly"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 사용자의 랭킹 정보를 찾을 수 없습니다."))
                .andDo(print())
        }
    }

}
