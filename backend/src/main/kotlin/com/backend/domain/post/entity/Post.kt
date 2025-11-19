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

}

