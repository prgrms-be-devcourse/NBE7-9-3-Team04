package com.backend.api.resume.dto.response

import com.backend.domain.resume.entity.Resume
import com.backend.domain.user.entity.User
import io.swagger.v3.oas.annotations.media.Schema


data class ResumeCreateResponse(
    @field:Schema(
        description = "이력서 ID",
        example = "1"
    ) val resumeId: Long,
    @field:Schema(
        description = "사용자 ID",
        example = "1"
    ) val userId: Long,
    @field:Schema(
        description = "이력서 내용",
        example = "이력서 내용입니다."
    ) val content: String?,
    @field:Schema(
        description = "기술 스택",
        example = "Java, Spring Boot"
    ) val skill: String?,
    @field:Schema(
        description = "대외 활동",
        example = "대외 활동 내용입니다."
    ) val activity: String?,
    @field:Schema(
        description = "자격증",
        example = "없음"
    ) val certification: String?,
    @field:Schema(
        description = "경력 사항",
        example = "경력 사항 내용입니다."
    ) val career: String?,
    @field:Schema(
        description = "포트폴리오 URL",
        example = "http://portfolio.example.com"
    ) val portfolioUrl: String?
) {
    companion object {
        fun from(resume: Resume, user: User): ResumeCreateResponse {
            return ResumeCreateResponse(
                resume.id,
                user.id,
                resume.content,
                resume.skill,
                resume.activity,
                resume.certification,
                resume.career,
                resume.portfolioUrl
            )
        }
    }
}
