package com.backend.global.lock

import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class DistributedLockManager(
    private val redissonClient: RedissonClient
) {

    private val log = LoggerFactory.getLogger(DistributedLockManager::class.java)

    fun <T> withLock(
        key: String, //락 이름
        waitTime: Long = 5, //락 기다리는 시간
        leaseTime: Long = 3, // 락 임대 시간. 이 시간 지나면 락을 해제한다.
        action: () -> T
    ): T {
        val lock = redissonClient.getLock(key)

        val acquiredLock = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)
        if (!acquiredLock) {
            log.error("$key not acquired")
            throw ErrorException(ErrorCode.LOCK_ACQUIRE_FAILED)
        }

        try {
            return action()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
                log.info("Lock released - key: $key")
            }
        }
    }
}