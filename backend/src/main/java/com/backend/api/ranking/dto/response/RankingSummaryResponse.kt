package com.backend.api.ranking.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@JvmRecord
data class RankingSummaryResponse(
    @field:Schema(description = "현재 로그인한 사용자의 랭킹 정보")
    val myRanking: RankingResponse,

    @field:Schema(description = "상위 10명의 랭킹 목록")
    val topRankings: List<RankingResponse>

) {
    companion object {
        fun from(myRanking: RankingResponse,
                 topRankings: List<RankingResponse>
        ): RankingSummaryResponse {
            return RankingSummaryResponse(myRanking, topRankings)
        }
    }
}