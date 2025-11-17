package com.backend.domain.feedback.repository

import com.backend.domain.feedback.entity.Feedback
import org.springframework.data.jpa.repository.JpaRepository

interface FeedbackRepository : JpaRepository<Feedback, Long> {
    fun findByAnswerId(answerId: Long): Feedback?
}
