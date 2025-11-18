package com.backend.domain.resume.entity

import com.backend.api.resume.dto.request.ResumeUpdateRequest
import com.backend.domain.user.entity.User
import com.backend.global.entity.BaseEntity
import jakarta.persistence.*


@Entity
class Resume(
    @Column(columnDefinition = "TEXT")
    var content: String?, // 이력서 내용

    var skill: String?, // 기술 스택

    @Column(columnDefinition = "TEXT")
    var activity: String?, // 대외 활동

    @Column(columnDefinition = "TEXT")
    var certification: String?, // 자격증

    @Column(columnDefinition = "TEXT")
    var career: String?, // 경력 사항

    var portfolioUrl: String?, // 포트폴리오 URL

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User // 이력서 소유자

) : BaseEntity() {

    fun update(request: ResumeUpdateRequest) {
        this.skill = request.skill
        this.activity = request.activity
        this.career = request.career
        this.certification = request.certification
        this.content = request.content
        this.portfolioUrl = request.portfolioUrl
    }

}
