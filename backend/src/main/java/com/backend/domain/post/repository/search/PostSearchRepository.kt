package com.backend.domain.post.repository.search

import com.backend.domain.post.entity.search.PostDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

@Repository
interface PostSearchRepository : ElasticsearchRepository<PostDocument, String>
