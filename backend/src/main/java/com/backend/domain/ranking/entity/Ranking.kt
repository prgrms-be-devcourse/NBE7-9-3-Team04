package com.backend.domain.ranking.entity

import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
class Ranking(

    @Column(nullable = false)
    var totalScore: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var tier: Tier,

    @Column(nullable = false)
    var rankValue: Int,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User
) : BaseEntity() {


    fun updateTotalScore(totalScore: Int) {
        this.totalScore = totalScore
    }

    fun updateTier(tier: Tier) {
        this.tier = tier
    }

    fun updateRank(rankValue: Int) {
        this.rankValue = rankValue
    }
}
