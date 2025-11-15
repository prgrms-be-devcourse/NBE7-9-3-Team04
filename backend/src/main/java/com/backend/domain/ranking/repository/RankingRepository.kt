package com.backend.domain.ranking.repository

import com.backend.domain.ranking.entity.Ranking
import com.backend.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface RankingRepository : JpaRepository<Ranking, Long> {
    fun findByUser(user: User?): Ranking?
    fun findByUserIdIn(userIds: List<Long>): List<Ranking>
    fun existsByUser(user: User): Boolean
}
