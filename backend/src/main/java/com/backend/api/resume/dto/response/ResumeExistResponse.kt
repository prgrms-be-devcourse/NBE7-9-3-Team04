package com.backend.api.resume.dto.response

import io.swagger.v3.oas.annotations.media.Schema


data class ResumeExistResponse(
    @field:Schema(
        description = "이력서 존재 여부",
        example = "true"
    ) val hasResume: Boolean
) {
    companion object {
        fun from(hasResume: Boolean): ResumeExistResponse {
            return ResumeExistResponse(hasResume)
        }
    }
}