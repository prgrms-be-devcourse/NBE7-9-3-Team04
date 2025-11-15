package com.backend.api.ranking.service

import com.backend.api.question.service.QuestionService
import com.backend.api.ranking.dto.response.RankingResponse
import com.backend.api.ranking.dto.response.RankingResponse.Companion.from
import com.backend.api.ranking.dto.response.RankingSummaryResponse
import com.backend.api.userQuestion.service.UserQuestionService
import com.backend.domain.ranking.entity.Ranking
import com.backend.domain.ranking.entity.Tier
import com.backend.domain.ranking.entity.Tier.Companion.fromScore
import com.backend.domain.ranking.repository.RankingRepository
import com.backend.domain.user.entity.User
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RankingService(
    private val rankingRepository: RankingRepository,
    private val userQuestionService: UserQuestionService,
    private val questionService: QuestionService,
    private val stringRedisTemplate: StringRedisTemplate
) {

    companion object {
        private const val REDIS_PREFIX = "ranking_"
    }

    @Transactional
    fun createRanking(user: User): Ranking {
        if (rankingRepository.existsByUser(user)) {
            throw ErrorException(ErrorCode.RANKING_ALREADY_EXISTS)
        }

        val ranking = Ranking(
            user = user,
            totalScore = 0,
            tier = Tier.UNRATED,
            rankValue = 0
        )

        return rankingRepository.save(ranking)
    }

    @Transactional
    fun updateUserRanking(user: User) {

        val totalScore = userQuestionService.getTotalUserQuestionScore(user)

        if (totalScore < 0) {
            throw ErrorException(ErrorCode.INVALID_SCORE)
        }

        val ranking: Ranking = rankingRepository.findByUser(user)
            ?: createRanking(user) // 존재하지 않으면 생성

        // 점수 / 티어 업데이트
        ranking.updateTotalScore(totalScore)
        ranking.updateTier(fromScore(totalScore))

        rankingRepository.save(ranking)

        stringRedisTemplate.opsForZSet().add(
            REDIS_PREFIX,
            user.id.toString(),
            totalScore.toDouble()
        )
    }

    //마이페이지용
    @Transactional(readOnly = true)
    fun getMyRanking(user: User): RankingResponse {

        val ranking: Ranking = rankingRepository.findByUser(user)
            ?: throw ErrorException(ErrorCode.RANKING_NOT_FOUND)

        var rankIndex = stringRedisTemplate.opsForZSet()
            .reverseRank(REDIS_PREFIX, user.id.toString())

        if(rankIndex == null) {
            stringRedisTemplate.opsForZSet().add(
                REDIS_PREFIX,
                user.id.toString(),
                ranking.totalScore.toDouble()
            )

            rankIndex = stringRedisTemplate.opsForZSet()
                .reverseRank(REDIS_PREFIX, user.id.toString())
                ?:throw ErrorException(ErrorCode.RANKING_NOT_AVAILABLE)
        }

        val rankValue = rankIndex.toInt() + 1

        val solvedCount = userQuestionService.countSolvedQuestion(user)
        val questionCount = questionService.countByUser(user)

        return from(ranking, rankValue, solvedCount, questionCount)
    }


    @Transactional(readOnly = true)
    fun getTopRankings(): List<RankingResponse> {

        val topRanks = stringRedisTemplate.opsForZSet()
            .reverseRangeWithScores(REDIS_PREFIX, 0, 9)
            ?: throw ErrorException(ErrorCode.RANKING_NOT_AVAILABLE)

        //Redis 결과에서 사용자 ID, 점수 추출
        val userIds =  topRanks.map { it.value!!.toLong() }

        val dbRankings: List<Ranking> = rankingRepository.findByUserIdIn(userIds)

        // Redis 순위와 DB 정보를 결합
        val rankingMap = dbRankings.associateBy { it.user.id }

        var rank = 1

        return topRanks.mapNotNull { tuple ->

            val userId = tuple.value!!.toLong()
            val ranking = rankingMap[userId] ?: return@mapNotNull null
            val solved = userQuestionService.countSolvedQuestion(ranking.user)
            val submitted = questionService.countByUser(ranking.user)

            from(ranking, rank++, solved, submitted)
        }
    }

    //상위 10명 + 내 랭킹
    @Transactional(readOnly = true)
    fun getRankingSummary(user: User): RankingSummaryResponse {
        val myRanking = getMyRanking(user)
        val topRankings = getTopRankings()

        return RankingSummaryResponse.from(myRanking, topRankings)
    }


}
