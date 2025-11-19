package com.backend.api.post.init

import com.backend.api.search.mapper.SearchDocumentMapper
import com.backend.domain.post.entity.Post
import com.backend.domain.post.repository.PostRepository
import com.backend.domain.post.repository.search.PostSearchRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Profile("!test")
@Component
class PostDataSyncRunner(
    private val postRepository: PostRepository,
    private val postSearchRepository: PostSearchRepository,
    private val mapper: SearchDocumentMapper
) {

    private val log = LoggerFactory.getLogger(PostDataSyncRunner::class.java)

    @PostConstruct
    fun init() {
        log.warn("▶▶▶ PostDataSyncRunner CREATED — Bean successfully initialized")
    }

    @Transactional(readOnly = true)
    @EventListener(ApplicationReadyEvent::class)
    fun sync() {
        log.warn("▶▶▶ PostDataSyncRunner SYNC TRIGGERED — ApplicationReadyEvent fired")

        try {
            log.info("Elasticsearch 게시글(Post) 인덱싱 시작")

            val posts: List<Post> = postRepository.findAll().toList()
            log.info("Loaded posts count = {}", posts.size)

            val docs = posts.map {
                try {
                    mapper.toPostDocument(it)
                } catch (e: Exception) {
                    log.error("ERROR mapping Post -> PostDocument (postId = ${it.id}): ${e.message}", e)
                    throw e
                }
            }

            log.info("Mapped PostDocument count = {}", docs.size)

            docs.chunked(500).forEachIndexed { idx, chunk ->
                log.info("Saving chunk #${idx + 1} (${chunk.size} docs)")
                postSearchRepository.saveAll(chunk)
            }

            log.info("Elasticsearch 게시글 인덱싱 완료: {}건", docs.size)

        } catch (e: Exception) {
            log.error("PostDataSyncRunner 전체 실패: ${e.message}", e)
            throw e
        }
    }
}
