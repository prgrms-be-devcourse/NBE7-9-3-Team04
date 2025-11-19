package com.backend.domain.review.repository

import com.backend.domain.review.entity.Review
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Long> {

    fun findByUserIdOrderByIdDesc(userId: Long): List<Review>
}
