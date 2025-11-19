package com.backend.api.ranking.controller

import com.backend.api.ranking.dto.response.RankingResponse
import com.backend.api.ranking.dto.response.RankingSummaryResponse
import com.backend.api.ranking.service.RankingService
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Ranking", description = "유저 랭킹 관련 API")
@RestController
@RequestMapping("/api/v1/rankings")
class RankingController(
    private val rankingService: RankingService,
    private val rq: Rq
) {

    @Operation(
        summary = "전체 랭킹 조회",
        description = "상위 10명의 랭킹과 현재 로그인 사용자의 랭킹을 함께 반환합니다."
    )
    @GetMapping
    fun getRankings(): ApiResponse<RankingSummaryResponse>{
        val user = rq.getUser()
        val response = rankingService.getRankingSummary(user)
        return ApiResponse.ok("전체 랭킹 조회를 성공했습니다.", response)
    }

    @Operation(summary = "내 랭킹 단독 조회", description = "현재 로그인한 사용자의 랭킹만 반환합니다.")
    @GetMapping("/me")
    fun getMyRankingOnly(): ApiResponse<RankingResponse>{
        val currentUser = rq.getUser()
        val myRanking = rankingService.getMyRanking(currentUser)
        return ApiResponse.ok("내 랭킹 조회를 성공했습니다.", myRanking)
    }
}
