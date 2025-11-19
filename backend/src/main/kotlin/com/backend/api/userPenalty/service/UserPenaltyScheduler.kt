package com.backend.domain.userPenalty.scheduler

import com.backend.api.user.event.publisher.UserStatusChangeEvent
import com.backend.domain.user.entity.AccountStatus
import com.backend.domain.user.repository.UserRepository
import com.backend.domain.userPenalty.entity.UserPenalty
import com.backend.domain.userPenalty.repository.UserPenaltyRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class UserPenaltyScheduler(
    private val userPenaltyRepository: UserPenaltyRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val log = LoggerFactory.getLogger(UserPenaltyScheduler::class.java)

    // 매일 00:05 실행 — 만료된 정지 사용자 ACTIVE로 복구
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    fun releaseExpiredPenalties() {
        val expiredPenalties: List<UserPenalty> =
            userPenaltyRepository.findExpiredPenalties(LocalDateTime.now())

        if (expiredPenalties.isEmpty()) {
            log.info("[Scheduler] 해제 대상 없음 ({}건)", expiredPenalties.size)
            return
        }

        log.info("[Scheduler] 해제 대상 감지: {}건", expiredPenalties.size)

        expiredPenalties.forEach { penalty ->
            try {
                processPenaltyRelease(penalty)
            } catch (e: Exception) {
                log.error(
                    "[Scheduler] 해제 처리 실패 (penaltyId={}, userId={}): {}",
                    penalty.id,
                    penalty.user?.id ?: "unknown",
                    e.message
                )
            }
        }
    }

    // 개별 정지 해제 처리 (트랜잭션 단위 분리)
    @Transactional
    protected fun processPenaltyRelease(penalty: UserPenalty) {
        val user = penalty.user ?: return

        if (!penalty.released && user.accountStatus == AccountStatus.SUSPENDED) {
            user.changeStatus(AccountStatus.ACTIVE)
            penalty.markReleased()

            userRepository.save(user)
            userPenaltyRepository.save(penalty)

            // 비동기 이메일 이벤트 발행
            eventPublisher.publishEvent(UserStatusChangeEvent(user, penalty))

            log.info(
                "[Scheduler] 정지 해제 완료: userId={}, penaltyId={}",
                user.id,
                penalty.id
            )
        }
    }
}
