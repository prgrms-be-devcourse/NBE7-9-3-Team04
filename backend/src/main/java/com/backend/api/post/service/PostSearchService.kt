package com.backend.api.post.service

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.PhrasePrefix
import com.backend.domain.post.entity.search.PostDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Service

@Service
class PostSearchService(
    private val operations: ElasticsearchOperations
) {

    fun search(keyword: String, page: Int, size: Int): Page<PostDocument> {
        val pageable = PageRequest.of(page.coerceAtLeast(1) - 1, size)

        val query = NativeQuery.builder().apply {
            withQuery {
                it.multiMatch { mm ->
                    mm.fields("title", "introduction", "content")
                        .query(keyword)
                        .type(PhrasePrefix)
                }
            }
            withPageable(pageable)
        }.build()

        val hits = operations.search(query, PostDocument::class.java)
        val content = hits.searchHits.map { it.content }

        return PageImpl(content, pageable, hits.totalHits)
    }
}

