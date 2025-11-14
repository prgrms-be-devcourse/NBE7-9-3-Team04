package com.backend.global.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val _id: Long? = null

    val id: Long
        get() = _id ?: throw IllegalStateException("엔티티의 ID 값이 존재하지 않습니다.")

    @CreatedDate
    var createDate: LocalDateTime? = null

    @LastModifiedDate
    var modifyDate: LocalDateTime? = null
}