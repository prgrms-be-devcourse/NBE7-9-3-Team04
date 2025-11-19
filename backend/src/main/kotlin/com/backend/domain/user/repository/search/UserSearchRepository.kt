package com.backend.domain.user.repository.search

import com.backend.domain.user.entity.search.UserDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

@Repository
interface UserSearchRepository : ElasticsearchRepository<UserDocument, String>
