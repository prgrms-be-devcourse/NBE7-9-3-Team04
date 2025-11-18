package com.backend.api.post.service

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.PhrasePrefix
import com.backend.domain.post.entity.search.PostDocument
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.stereotype.Service

@Service
class PostSearchService(
    private val operations: ElasticsearchOperations
) {

    fun search(keyword: String, page: Int, size: Int): Page<PostDocument> {
        val pageNum = if (page < 1) 1 else page
        val pageable: Pageable = PageRequest.of(pageNum - 1, size)

        // multiMatch + phrase_prefix 검색 (제목 + 소개 + 본문)
        val query: NativeQuery = NativeQueryBuilder()
            .withQuery { q ->
                q.multiMatch { mm ->
                    mm.fields("title", "introduction", "content")
                        .query(keyword)
                        .type(PhrasePrefix)
                }
            }
            .withPageable(pageable)
            .build()

        val hits: SearchHits<PostDocument> =
            operations.search(query, PostDocument::class.java)

        val content: List<PostDocument> =
            hits.searchHits.map(SearchHit<PostDocument>::getContent)

        return PageImpl(content, pageable, hits.totalHits)
    }
}
