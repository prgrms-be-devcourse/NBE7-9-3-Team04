package com.backend.domain.user.repository.search

import com.backend.domain.user.entity.search.UserDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface UserSearchRepository : ElasticsearchRepository<UserDocument, String>
