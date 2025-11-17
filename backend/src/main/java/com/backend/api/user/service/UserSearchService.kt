package com.backend.api.user.service

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.PhrasePrefix
import com.backend.domain.user.entity.search.UserDocument
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
class UserSearchService(
    private val operations: ElasticsearchOperations
) {

    fun search(keyword: String, page: Int, size: Int): Page<UserDocument> {
        val pageNum = if (page < 1) 1 else page
        val pageable: Pageable = PageRequest.of(pageNum - 1, size)

        val query: NativeQuery = NativeQueryBuilder()
            .withQuery { q ->
                q.bool { b ->
                    b.should { s ->
                        s.multiMatch { mm ->
                            mm.fields("name", "nickname")
                                .query(keyword)
                                .type(PhrasePrefix)
                        }
                    }.should { s ->
                        s.queryString { qs ->
                            qs.fields("name", "nickname")
                                .query("*${escape(keyword)}*")
                        }
                    }
                }
            }
            .withPageable(pageable)
            .build()

        val hits: SearchHits<UserDocument> =
            operations.search(query, UserDocument::class.java)

        val content: List<UserDocument> = hits.searchHits.map(SearchHit<UserDocument>::getContent)

        return PageImpl(content, pageable, hits.totalHits)
    }

    private fun escape(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace("+", "\\+")
            .replace("-", "\\-")
            .replace("=", "\\=")
            .replace("&&", "\\&&")
            .replace("||", "\\||")
            .replace(">", "\\>")
            .replace("<", "\\<")
            .replace("!", "\\!")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("^", "\\^")
            .replace("\"", "\\\"")
            .replace("~", "\\~")
            .replace("*", "\\*")
            .replace("?", "\\?")
            .replace(":", "\\:")
            .replace("/", "\\/")
    }
}
