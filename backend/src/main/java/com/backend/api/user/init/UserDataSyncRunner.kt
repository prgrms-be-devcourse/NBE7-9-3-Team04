package com.backend.api.user.init

import com.backend.domain.user.entity.search.UserDocument
import com.backend.domain.user.repository.UserRepository
import com.backend.domain.user.repository.search.UserSearchRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class UserDataSyncRunner(
    private val userRepository: UserRepository,
    private val userSearchRepository: UserSearchRepository
) {

    private val log = LoggerFactory.getLogger(UserDataSyncRunner::class.java)

    @Async
    @EventListener(ApplicationReadyEvent::class)
    fun sync() {
        log.info("Elasticsearch 사용자 인덱싱 시작")

        val users = userRepository.findAll()
        val docs = users.map { UserDocument.from(it) }

        docs.chunked(500).forEach { chunk ->
            userSearchRepository.saveAll(chunk)
        }

        log.info("Elasticsearch 사용자 인덱싱 완료: {}건", docs.size)
    }
}
