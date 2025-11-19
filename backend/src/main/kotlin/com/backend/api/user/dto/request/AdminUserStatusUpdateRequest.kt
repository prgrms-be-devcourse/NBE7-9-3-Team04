package com.backend.api.user.dto.request

import com.backend.domain.user.entity.AccountStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class AdminUserStatusUpdateRequest(
    @field:NotNull(message = "계정 상태는 필수입니다.")
    @field:Schema(description = "변경할 계정 상태", example = "SUSPENDED")
    val status: AccountStatus,

     @field:Schema(
        description = "정지 사유 (예: 부적절한 게시물 등록, 신고 누적 등)",
        example = "신고 누적에 따른 일시 정지"
    ) val reason: String?= null,

    @field:Schema(
        description = "정지 해제 예정일 (영구 정지는 null)",
        example = "2025-11-30T00:00:00"
    ) val suspendEndDate: LocalDate?= null,


) {
    fun clearReasonAndDate(): AdminUserStatusUpdateRequest {
        return AdminUserStatusUpdateRequest(status, null, null)
    }
}
