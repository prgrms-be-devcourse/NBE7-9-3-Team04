package com.backend.api.ranking.dto.response

import com.backend.domain.ranking.entity.Ranking
import com.backend.domain.ranking.entity.Tier
import io.swagger.v3.oas.annotations.media.Schema


data class RankingResponse(

    @field:Schema(description = "사용자 ID", example = "12")
    val userId: Long,

    @field:Schema(description = "닉네임", example = "김개발")
    val nickName: String,

    @field:Schema(description = "이메일", example = "kim@example.com")
    val email: String,

    @field:Schema(description = "총 점수", example = "870")
    val totalScore: Int,

    @field:Schema(description = "현재 티어", example = "GOLD")
    val currentTier: Tier,

    @field:Schema(description = "순위", example = "15")
    val rankValue: Int,

    @field:Schema(description = "다음 티어", example = "PLATINUM")
    val nextTier: Tier?,

    @field:Schema(description = "다음 티어까지 남은 점수", example = "130")
    val scoreToNextTier: Int,

    @field:Schema(description = "해결한 문제 수", example = "100")
    val solvedCount: Int,

    @field:Schema(description = "제출한 질문 수", example = "5")
    val questionCount: Int
) {
    companion object {
        fun from(ranking: Ranking,
                 rankValue: Int,
                 solvedCount: Int,
                 questionCount: Int
        ): RankingResponse {
            val totalScore= ranking.totalScore
            val currentTier = ranking.tier
            val nextTier = currentTier.nextTier()

            val scoreToNextTier =
                if(currentTier == Tier.MASTER) 0
                else nextTier.minScore - totalScore

            return RankingResponse(
                ranking.user.id,
                ranking.user.nickname,
                ranking.user.email,
                totalScore,
                currentTier,
                rankValue,
                nextTier,
                scoreToNextTier,
                solvedCount,
                questionCount
            )
        }
    }
}
