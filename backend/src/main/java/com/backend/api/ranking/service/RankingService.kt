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
import com.backend.global.lock.DistributedLockManager
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

@Service
class RankingService(
    private val rankingRepository: RankingRepository,
    private val userQuestionService: UserQuestionService,
    private val questionService: QuestionService,
    private val stringRedisTemplate: StringRedisTemplate,
    private val lockManager: DistributedLockManager,
    private val transactionTemplate: TransactionTemplate
) {

    companion object {
        private const val REDIS_PREFIX = "ranking_"
    }

    /*
    락 획득 -> 트랜잭션 시작 -> DB/Redis 업데이트 -> 트랜잭션 커밋 -> 락 해제
     */
    fun updateUserRanking(user: User) {

        val lockKey = "lock:ranking:user:${user.id}"

        lockManager.withLock(lockKey){

            transactionTemplate.execute {
                val totalScore = userQuestionService.getTotalUserQuestionScore(user)
                if (totalScore < 0) {
                    throw ErrorException(ErrorCode.INVALID_SCORE)
                }

                val ranking = rankingRepository.findByUser(user)
                    ?: Ranking(
                        user = user,
                        totalScore = 0,
                        tier = Tier.UNRATED,
                        rankValue = 0
                    ).also { rankingRepository.save(it) }

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
        }
    }

    private fun loadSortedRankings(): List<Pair<Long, Double>> {

        val tuples = stringRedisTemplate.opsForZSet()
            .reverseRangeWithScores(REDIS_PREFIX, 0, -1)
            ?: throw ErrorException(ErrorCode.RANKING_NOT_AVAILABLE)

        val userIds = tuples.map { it.value!!.toLong() }
        val dbRankings = rankingRepository.findByUserIdIn(userIds)

        val rankingMap = dbRankings.associateBy { it.user.id }

        // 점수 DESC → 닉네임 ASC 정렬
        return tuples.sortedWith(
            compareByDescending<ZSetOperations.TypedTuple<String>> { it.score }
                .thenBy { tuple ->
                    rankingMap[tuple.value!!.toLong()]!!.user.nickname
                }
        ).map { tuple ->
            tuple.value!!.toLong() to tuple.score!!
        }
    }


    @Transactional(readOnly = true)
    fun getMyRanking(user: User): RankingResponse {

        val ranking = rankingRepository.findByUser(user)
            ?: throw ErrorException(ErrorCode.RANKING_NOT_FOUND)

        // Redis에 없으면 추가
        val redisScore = stringRedisTemplate.opsForZSet()
            .score(REDIS_PREFIX, user.id.toString())

        if (redisScore == null) {
            stringRedisTemplate.opsForZSet().add(
                REDIS_PREFIX,
                user.id.toString(),
                ranking.totalScore.toDouble()
            )
        }

        val sorted = loadSortedRankings()

        val myIndex = sorted.indexOfFirst { it.first == user.id }
        if (myIndex == -1) throw ErrorException(ErrorCode.RANKING_NOT_AVAILABLE)

        val rankValue = myIndex + 1

        val solvedCount = userQuestionService.countSolvedQuestion(user)
        val questionCount = questionService.countByUser(user)

        return from(ranking, rankValue, solvedCount, questionCount)
    }


    @Transactional(readOnly = true)
    fun getTopRankings(): List<RankingResponse> {

        val sorted = loadSortedRankings()

        // Top 10 userId 추출
        val top10 = sorted.take(10).map { it.first }

        val dbRankings = rankingRepository.findByUserIdIn(top10)
        val rankingMap = dbRankings.associateBy { it.user.id }

        return top10.mapIndexed { index, userId ->
            val ranking = rankingMap[userId] ?: return@mapIndexed null
            val solved = userQuestionService.countSolvedQuestion(ranking.user)
            val submitted = questionService.countByUser(ranking.user)

            from(ranking, index + 1, solved, submitted)
        }.filterNotNull()
    }

    //상위 10명 + 내 랭킹
    @Transactional(readOnly = true)
    fun getRankingSummary(user: User): RankingSummaryResponse {
        val myRanking = getMyRanking(user)
        val topRankings = getTopRankings()

        return RankingSummaryResponse.from(myRanking, topRankings)
    }


}
