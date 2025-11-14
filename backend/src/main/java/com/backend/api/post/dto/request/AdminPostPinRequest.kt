package com.backend.api.post.dto.request

import com.backend.domain.post.entity.PinStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@JvmRecord
data class AdminPostPinRequest(
    @Schema(description = "상단 고정 여부", example = "PINNED")
    @field:NotNull(message = "상단 고정 상태는 필수입니다.")
    val pinStatus: PinStatus?
)