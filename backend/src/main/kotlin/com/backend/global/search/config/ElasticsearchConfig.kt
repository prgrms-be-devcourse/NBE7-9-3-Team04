package com.backend.global.search.config

import com.backend.domain.post.entity.search.PostDocument
import com.backend.domain.user.entity.search.UserDocument
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.document.Document
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Configuration
@Profile("!test")
@EnableElasticsearchRepositories(basePackages = ["com.backend.domain.*.repository.search"])
class ElasticsearchConfig(

    @Value("\${spring.elasticsearch.uris}")
    private val elasticsearchUrl: String

) : ElasticsearchConfiguration() {

    override fun clientConfiguration(): ClientConfiguration {
        return ClientConfiguration.builder()
            .connectedTo(elasticsearchUrl.replace("http://", ""))
            .build()
    }

    private val noriSettings = mapOf(
        "analysis" to mapOf(
            "tokenizer" to mapOf(
                "nori_tokenizer" to mapOf("type" to "nori_tokenizer")
            ),
            "analyzer" to mapOf(
                "nori_analyzer" to mapOf(
                    "type" to "custom",
                    "tokenizer" to "nori_tokenizer",
                    "filter" to listOf("lowercase")
                )
            )
        )
    )

    @Bean
    fun createUsersIndex(operations: ElasticsearchOperations): CommandLineRunner {
        return CommandLineRunner {
            val indexOps = operations.indexOps(UserDocument::class.java)

            val mapping = Document.from(
                mapOf(
                    "properties" to mapOf(
                        "id" to mapOf("type" to "keyword"),
                        "name" to mapOf("type" to "text", "analyzer" to "nori_analyzer"),
                        "nickname" to mapOf("type" to "text", "analyzer" to "nori_analyzer"),
                        "email" to mapOf("type" to "keyword"),
                        "role" to mapOf("type" to "keyword")
                    )
                )
            )

            if (!indexOps.exists()) {
                indexOps.create(noriSettings)
                indexOps.putMapping(mapping)
                println("[ES] users 인덱스 생성 완료")
            } else {
                println("[ES] users 인덱스 이미 존재")
            }
        }
    }

    @Bean
    fun createPostsIndex(operations: ElasticsearchOperations): CommandLineRunner {
        return CommandLineRunner {
            val indexOps = operations.indexOps(PostDocument::class.java)

            val mapping = Document.from(
                mapOf(
                    "properties" to mapOf(
                        "id" to mapOf("type" to "keyword"),
                        "title" to mapOf("type" to "text", "analyzer" to "nori_analyzer"),
                        "introduction" to mapOf("type" to "text", "analyzer" to "nori_analyzer"),
                        "content" to mapOf("type" to "text", "analyzer" to "nori_analyzer"),
                        "postCategoryType" to mapOf("type" to "keyword"),
                        "authorNickname" to mapOf("type" to "keyword"),
                        "recruitCount" to mapOf("type" to "integer"),
                        "deadline" to mapOf(
                            "type" to "date",
                            "format" to "strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss"
                        ),
                        "createdDate" to mapOf(
                            "type" to "date",
                            "format" to "strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss"
                        ),
                        "modifyDate" to mapOf(
                            "type" to "date",
                            "format" to "strict_date_optional_time||yyyy-MM-dd'T'HH:mm:ss.SSS||yyyy-MM-dd'T'HH:mm:ss"
                        ),
                        "status" to mapOf("type" to "keyword"),
                        "pinStatus" to mapOf("type" to "keyword")
                    )
                )
            )

            if (!indexOps.exists()) {
                indexOps.create(noriSettings)
                indexOps.putMapping(mapping)
                println("[ES] posts 인덱스 생성 완료")
            } else {
                println("[ES] posts 인덱스 이미 존재")
            }
        }
    }
}
