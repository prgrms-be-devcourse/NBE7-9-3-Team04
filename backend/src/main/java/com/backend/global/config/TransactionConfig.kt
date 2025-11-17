package com.backend.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

//락, 트랜잭션 순서를 보장하기 위해 도입
@Configuration
class TransactionConfig(
    private val transactionManager: PlatformTransactionManager
) {

    @Bean
    fun transactionTemplate(): TransactionTemplate {
        return TransactionTemplate(transactionManager)
    }
}