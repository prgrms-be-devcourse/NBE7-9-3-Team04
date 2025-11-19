package com.backend.domain.userPenalty.repository

import com.backend.domain.userPenalty.entity.QUserPenalty
import com.backend.domain.userPenalty.entity.UserPenalty
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class UserPenaltyRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : UserPenaltyRepositoryCustom {

    override fun findExpiredPenalties(now: LocalDateTime): List<UserPenalty> {
        val p = QUserPenalty.userPenalty

        return queryFactory
            .selectFrom(p)
            .where(
                p.released.eq(false),
                p.endAt.isNotNull,
                p.endAt.loe(now)
            )
            .fetch()
    }
}