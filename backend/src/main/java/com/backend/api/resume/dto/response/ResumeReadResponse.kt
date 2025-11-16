package com.backend.api.resume.dto.response

import com.backend.domain.resume.entity.Resume


data class ResumeReadResponse(
    val id: Long,
    val skill: String?,
    val activity: String?,
    val career: String?,
    val certification: String?,
    val content: String?,
    val portfolioUrl: String?,
    val userId: Long
) {
    companion object {
        fun from(resume: Resume): ResumeReadResponse {
            return ResumeReadResponse(
                resume.id,
                resume.skill,
                resume.activity,
                resume.career,
                resume.certification,
                resume.content,
                resume.portfolioUrl,
                resume.user.id
            )
        }
    }
}
