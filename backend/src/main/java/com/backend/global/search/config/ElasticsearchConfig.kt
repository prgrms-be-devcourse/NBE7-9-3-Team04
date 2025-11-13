package com.backend.global.search.config

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
@EnableElasticsearchRepositories(basePackages = ["com.backend.domain"])
class ElasticsearchConfig(

    @Value("\${spring.elasticsearch.uris}")
    private val elasticsearchUrl: String

) : ElasticsearchConfiguration() {

    override fun clientConfiguration(): ClientConfiguration {
        return ClientConfiguration.builder()
            .connectedTo(elasticsearchUrl.replace("http://", ""))
            .build()
    }

    @Bean
    fun createUsersIndex(operations: ElasticsearchOperations): CommandLineRunner {
        return CommandLineRunner {
            val indexOps = operations.indexOps(UserDocument::class.java)

            val settings = mapOf(
                "analysis" to mapOf(
                    "analyzer" to mapOf(
                        "nori_analyzer" to mapOf(
                            "type" to "custom",
                            "tokenizer" to "nori_tokenizer",
                            "filter" to listOf("nori_readingform", "lowercase")
                        )
                    )
                )
            )

            val mapping: Document = indexOps.createMapping(UserDocument::class.java)

            if (!indexOps.exists()) {
                indexOps.create(settings)
                indexOps.putMapping(mapping)
                println("[Elasticsearch] users 인덱스 생성 완료")
            } else {
                println("[Elasticsearch] users 인덱스 이미 존재")
            }
        }
    }
}
