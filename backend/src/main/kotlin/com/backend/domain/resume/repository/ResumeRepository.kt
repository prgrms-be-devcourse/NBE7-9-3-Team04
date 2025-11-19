package com.backend.domain.resume.repository

import com.backend.domain.resume.entity.Resume
import com.backend.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface ResumeRepository : JpaRepository<Resume, Long> {
    fun existsByUserId(userId: Long): Boolean
    fun findByUser(user: User): Resume?
}
