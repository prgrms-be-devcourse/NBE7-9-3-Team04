package com.backend.api.user.controller

import com.backend.api.user.dto.request.AdminUserStatusUpdateRequest
import com.backend.api.user.dto.response.AdminUserResponse
import com.backend.api.user.dto.response.UserPageResponse
import com.backend.api.user.service.AdminUserService
import com.backend.domain.user.entity.User
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin User", description = "사용자 관리 API(관리자)")
class AdminUserController(
    private val adminUserService: AdminUserService,
    private val rq: Rq
) {
    fun getCurrentUser(): User = rq.getUser()

    @GetMapping
    @Operation(summary = "전체 사용자 조회", description = "모든 사용자 정보를 조회합니다.")
    fun getAllUsers(
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<UserPageResponse<AdminUserResponse>> {
        val admin = getCurrentUser()
        val usersPage = adminUserService.getAllUsers(page, admin)
        return ApiResponse.ok("전체 사용자 조회 성공", usersPage)
    }

    @GetMapping("/{userId}")
    @Operation(summary = "특정 사용자 조회", description = "특정 사용자 정보를 조회합니다.")
    fun getUserById(
        @PathVariable userId: Long
    ): ApiResponse<AdminUserResponse> {
        val admin = getCurrentUser()
        val user = adminUserService.getUserById(userId, admin)
        return ApiResponse.ok("특정 사용자 조회 성공", user)
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "사용자 상태 변경", description = "관리자가 특정 사용자의 계정 상태를 변경합니다.")
    fun changeUserStatus(
        @PathVariable userId: Long,
        @RequestBody request: @Valid AdminUserStatusUpdateRequest
    ): ApiResponse<AdminUserResponse> {
        val admin = getCurrentUser()
        val response = adminUserService.changeUserStatus(userId, request, admin)
        return ApiResponse.ok("사용자 상태 변경 성공", response)
    }
}
