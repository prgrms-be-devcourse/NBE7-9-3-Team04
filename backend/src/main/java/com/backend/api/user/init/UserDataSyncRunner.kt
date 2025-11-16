package com.backend.api.user.init

import com.backend.domain.user.entity.search.UserDocument
import com.backend.domain.user.repository.UserRepository
import com.backend.domain.user.repository.search.UserSearchRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class UserDataSyncRunner(
    private val userRepository: UserRepository,
    private val userSearchRepository: UserSearchRepository
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(UserDataSyncRunner::class.java)

    override fun run(vararg args: String?) {
        val users = userRepository.findAll()

        val docs = users.map { u ->
            UserDocument.from(u)
        }

        userSearchRepository.saveAll(docs)
        log.info("Elasticsearch 인덱싱 완료: {}건 저장됨", docs.size)
    }
}
