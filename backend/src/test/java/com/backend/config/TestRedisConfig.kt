package com.backend.config

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import redis.embedded.RedisServer
import java.net.ServerSocket


@Configuration
@Profile("test")
class TestRedisConfig {
    private var redisServer: RedisServer? = null
    private var redisPort:Int = 0

    @PostConstruct
    fun startRedis() {
        redisPort = findAvailableTcpPort()
        redisServer = RedisServer(redisPort).also { it.start() }

        // Spring Boot Redis 설정에 반영
        System.setProperty("spring.data.redis.host", "localhost")
        System.setProperty("spring.data.redis.port", redisPort.toString())

        println("Embedded Redis 서버 시작 (port: ${redisPort})")
    }

    @PreDestroy
    fun stopRedis() {
        redisServer?.takeIf { it.isActive }?.stop()
        println("Embedded Redis 서버 종료")
    }


    private fun findAvailableTcpPort(): Int =
        ServerSocket(0).use { socket ->
            socket.reuseAddress = true
            socket.localPort

    }
}