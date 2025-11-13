package com.backend.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {

    @Bean(name = ["feedbackExecutor"])
    fun feedbackExecutor(): Executor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 5
        maxPoolSize = 10
        queueCapacity = 100
        setThreadNamePrefix("feedback-")
        initialize()
    }

    @Bean(name = ["mailExecutor"])
    fun mailExecutor(): Executor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 3       // 기본 스레드 수
        maxPoolSize = 10       // 최대 스레드 수
        queueCapacity = 50     // 큐 용량
        setThreadNamePrefix("MailExecutor-")
        initialize()
    }
}
