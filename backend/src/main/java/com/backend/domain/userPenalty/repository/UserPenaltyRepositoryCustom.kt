package com.backend.domain.userPenalty.repository

import com.backend.domain.userPenalty.entity.UserPenalty
import java.time.LocalDateTime

interface UserPenaltyRepositoryCustom {

    // 만료된 정지 내역 조회 (자동 해제용)
    fun findExpiredPenalties(now: LocalDateTime): List<UserPenalty>
}
