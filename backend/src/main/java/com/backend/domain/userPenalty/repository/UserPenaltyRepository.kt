package com.backend.domain.userPenalty.repository

import com.backend.domain.userPenalty.entity.UserPenalty
import org.springframework.data.jpa.repository.JpaRepository

interface UserPenaltyRepository :
    JpaRepository<UserPenalty, Long>,
    UserPenaltyRepositoryCustom {

    // 특정 유저의 정지 이력 전체 조회 (관리자용)
    fun findByUserIdOrderByStartAtDesc(userId: Long): List<UserPenalty>

    // 가장 최근 정지 이력 (이메일 발송 시 등)
    fun findTopByUserIdOrderByStartAtDesc(userId: Long): UserPenalty?
}
