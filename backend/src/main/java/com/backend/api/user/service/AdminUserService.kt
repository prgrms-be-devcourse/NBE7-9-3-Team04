package com.backend.api.user.service

import com.backend.api.user.dto.request.AdminUserStatusUpdateRequest
import com.backend.api.user.dto.response.AdminUserResponse
import com.backend.api.user.dto.response.AdminUserResponse.Companion.from
import com.backend.api.user.dto.response.UserPageResponse
import com.backend.domain.user.entity.AccountStatus
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.domain.userPenalty.entity.UserPenalty
import com.backend.domain.userPenalty.repository.UserPenaltyRepository
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Service
@Transactional(readOnly = true)
class AdminUserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val userPenaltyRepository: UserPenaltyRepository,
    private val mailExecutor: Executor
) {

    // 관리자 권한 검증
    fun validateAdminAuthority(user: User?) {
        if (user == null) {
            throw ErrorException(ErrorCode.UNAUTHORIZED_USER);
        }
        if (user.role != Role.ADMIN) {
            throw ErrorException(ErrorCode.FORBIDDEN)
        }
    }

    // 사용자 존재 여부 검증
    fun findByIdOrThrow(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_USER) }
    }

    // 전체 사용자 조회
    fun getAllUsers(page: Int, admin: User?): UserPageResponse<AdminUserResponse> {
        validateAdminAuthority(admin)

        val safePage = if (page < 1) 1 else page
        val pageable = PageRequest.of(safePage - 1, 15, Sort.by("createDate").descending())
        val usersPage = userRepository.findAllByRoleNot(Role.ADMIN, pageable)

        if (usersPage.isEmpty) {
            throw ErrorException(ErrorCode.NOT_FOUND_USER)
        }

        val users = usersPage.content.map { from(it) }

        return UserPageResponse.from(usersPage, users)
    }

    // 사용자 목록을 응답 DTO 리스트로 매핑
    fun mapToResponseList(usersPage: Page<User>): List<AdminUserResponse> {
        return usersPage.content.map{user -> AdminUserResponse.from(user) }
    }

    // 특정 사용자 조회
    fun getUserById(userId: Long, admin: User): AdminUserResponse {
        validateAdminAuthority(admin)
        val user = findByIdOrThrow(userId)
        return from(user)
    }

    @Transactional
    fun changeUserStatus(userId: Long, request: AdminUserStatusUpdateRequest, admin: User): AdminUserResponse {
        validateAdminAuthority(admin)
        val user = findByIdOrThrow(userId)
        validateNotDuplicateStatus(user, request.status)

        validateStatusChangeRequest(request) // 상태별 검증 메서드로 분리

        user.changeStatus(request.status)
        userRepository.saveAndFlush(user)

        val penalty = handleUserPenalty(user, request) // 패널티 로직 분리
        sendStatusChangeMailAsync(user, penalty) // 비동기 메일 전송 분리

        return from(user)
    }

    fun validateStatusChangeRequest(request: AdminUserStatusUpdateRequest) {
        when (request.status) {
            AccountStatus.SUSPENDED -> validateSuspendRequest(request)
            AccountStatus.BANNED -> validateBanRequest(request)
            AccountStatus.ACTIVE -> request.clearReasonAndDate()
            else -> throw ErrorException(ErrorCode.INVALID_STATUS)
        }
    }

    fun validateSuspendRequest(request: AdminUserStatusUpdateRequest) {
        if (request.reason.isNullOrBlank()) {
            throw ErrorException(ErrorCode.INVALID_SUSPEND_REASON)
        }
        if (request.suspendEndDate == null || request.suspendEndDate.isBefore(LocalDate.now())) {
            throw ErrorException(ErrorCode.INVALID_SUSPEND_PERIOD)
        }
    }

    fun validateBanRequest(request: AdminUserStatusUpdateRequest) {
        if (request.reason.isNullOrBlank()) {
            throw ErrorException(ErrorCode.INVALID_SUSPEND_REASON)
        }
        if (request.suspendEndDate != null) {
            throw ErrorException(ErrorCode.INVALID_BAN_PERIOD)
        }
    }

    // Penalty 기록 처리
    fun handleUserPenalty(user: User, request: AdminUserStatusUpdateRequest): UserPenalty? {
        if (request.status == AccountStatus.SUSPENDED || request.status == AccountStatus.BANNED) {
            val penalty = UserPenalty(
                user = user,
                reason = request.reason,
                startAt = LocalDateTime.now(),
                endAt = request.suspendEndDate?.atStartOfDay(),
                released = false,
                appliedStatus = request.status
            )
            return userPenaltyRepository.saveAndFlush(penalty)
        }
        return null
    }

    // 중복 상태 변경 방지
    fun validateNotDuplicateStatus(user: User, newStatus: AccountStatus) {
        if (user.accountStatus == newStatus) {
            throw ErrorException(ErrorCode.DUPLICATE_STATUS)
        }
    }

    // 메일 전송 비동기 처리
    fun sendStatusChangeMailAsync(user: User, penalty: UserPenalty?) {
        CompletableFuture.runAsync( {
            try {
                emailService.sendStatusChangeMail(user, penalty)
            } catch (e: Exception) {
                System.err.println("[메일 전송 실패] " + user.email + " - " + e.message)
            }
        }, mailExecutor)
    }
}
