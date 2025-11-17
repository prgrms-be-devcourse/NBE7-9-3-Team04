package com.backend.domain.post.entity

import com.backend.domain.comment.entity.Comment
import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
class Post(

    @field:Size(min = 2, max = 255)
    var title: String,// 제목

    @field:Size(min = 2)
    var introduction: String,// 한 줄 소개

    @field:Size(min = 10, max = 5000)
    var content: String,// 내용

    @field:NotNull
    var deadline: LocalDateTime, // 마감일

    @field:Enumerated(EnumType.STRING)
    var status: PostStatus, // 진행상태

    @field:Enumerated(EnumType.STRING)
    var pinStatus: PinStatus,// 상단 고정 여부


    var recruitCount: Int, // 모집 인원


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val users: User, // 게시글 작성자 ID


    @field:Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    var postCategoryType: PostCategoryType

) : BaseEntity() {

    @OneToMany(
        mappedBy = "post",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL]
    )
    private val comments: MutableList<Comment> = mutableListOf()

    init {
        validateDeadline(this.deadline)
    }

    private fun validateDeadline(deadline: LocalDateTime) {
        if (deadline.isBefore(LocalDateTime.now())) {
            throw ErrorException(ErrorCode.INVALID_DEADLINE)
        }
    }

    fun updatePost(
        title: String,
        introduction: String,
        content: String,
        deadline: LocalDateTime,
        status: PostStatus,
        pinStatus: PinStatus,
        recruitCount: Int,
        postCategoryType: PostCategoryType
    ) {
        this.title = title
        this.introduction = introduction
        this.content = content
        validateDeadline(deadline)
        this.deadline = deadline
        this.status = status
        this.pinStatus = pinStatus
        this.recruitCount = recruitCount
        this.postCategoryType = postCategoryType
    }

    fun updatePinStatus(pinStatus: PinStatus) {
        this.pinStatus = pinStatus
    }

    fun updateStatus(status: PostStatus) {
        this.status = status
    }

    fun getComments(): List<Comment> = this.comments

    // 임시 builder
    class Builder {
        private var title: String = ""
        private var introduction: String = ""
        private var content: String = ""
        private var deadline: LocalDateTime = LocalDateTime.now().plusDays(7)
        private var status: PostStatus = PostStatus.ING
        private var pinStatus: PinStatus = PinStatus.PINNED
        private var recruitCount: Int = 0
        private lateinit var users: User
        private var postCategoryType: PostCategoryType = PostCategoryType.PROJECT

        fun title(title: String) = apply { this.title = title }
        fun introduction(introduction: String) = apply { this.introduction = introduction }
        fun content(content: String) = apply { this.content = content }
        fun deadline(deadline: LocalDateTime) = apply { this.deadline = deadline }
        fun status(status: PostStatus) = apply { this.status = status }
        fun pinStatus(pinStatus: PinStatus) = apply { this.pinStatus = pinStatus }
        fun recruitCount(recruitCount: Int) = apply { this.recruitCount = recruitCount }
        fun users(users: User) = apply { this.users = users }
        fun postCategoryType(postCategoryType: PostCategoryType) = apply { this.postCategoryType = postCategoryType }

        fun build(): Post =
            Post(
                title = title,
                introduction = introduction,
                content = content,
                deadline = deadline,
                status = status,
                pinStatus = pinStatus,
                recruitCount = recruitCount,
                users = users,
                postCategoryType = postCategoryType
            )
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}

