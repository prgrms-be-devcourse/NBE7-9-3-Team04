package com.backend.global.lock

import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class DistributedLockManager(
    private val redissonClient: RedissonClient
) {

    suspend fun <T> withLock(
        key: String, //락 이름
        waitTime: Long = 5, //락 기다리는 시간
        leaseTime: Long = 3, // 락 임대 시간. 이 시간 지나면 락을 해제한다.
        block: suspend () -> T
    ) :T {
        val lock = redissonClient.getLock(key)

        val acquiredLock = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)
        if(!acquiredLock) {
            throw ErrorException(ErrorCode.LOCK_ACQUIRE_FAILED)
        }

        try{
            return block()
        }finally {
            if(lock.isHeldByCurrentThread){
                lock.unlock()
            }
        }
    }
    fun <T> withLockSync(
        key: String,
        waitTime: Long = 5,
        leaseTime: Long = 3,
        block: () -> T
    ): T {
        val lock = redissonClient.getLock(key)

        val locked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)
        if (!locked) {
            throw ErrorException(ErrorCode.LOCK_ACQUIRE_FAILED)
        }

        try {
            return block()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }
}