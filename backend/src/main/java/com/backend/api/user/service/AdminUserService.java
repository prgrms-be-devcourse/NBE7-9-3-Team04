package com.backend.api.user.service;

import com.backend.api.user.dto.request.AdminUserStatusUpdateRequest;
import com.backend.api.user.dto.response.AdminUserResponse;
import com.backend.api.user.dto.response.UserPageResponse;
import com.backend.api.user.event.publisher.UserStatusChangeEvent;
import com.backend.domain.user.entity.AccountStatus;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;
import com.backend.domain.userPenalty.entity.UserPenalty;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.userPenalty.repository.UserPenaltyRepository;
import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserPenaltyRepository userPenaltyRepository;
    private final Executor mailExecutor;

    // 관리자 권한 검증
    public void validateAdminAuthority(User user) {
        if (user == null) {
            throw new ErrorException(ErrorCode.UNAUTHORIZED_USER);
        }
        if (user.getRole() != Role.ADMIN) {
            throw new ErrorException(ErrorCode.FORBIDDEN);
        }
    }

    // 사용자 존재 여부 검증
    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ErrorException(ErrorCode.NOT_FOUND_USER));
    }

    // 전체 사용자 조회
    public UserPageResponse<AdminUserResponse> getAllUsers(int page, User admin) {
        validateAdminAuthority(admin);

        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, 15, Sort.by("createDate").descending());
        Page<User> usersPage = userRepository.findAllByRoleNot(Role.ADMIN, pageable);

        if (usersPage.isEmpty()) {
            throw new ErrorException(ErrorCode.NOT_FOUND_USER);
        }

        List<AdminUserResponse> users = mapToResponseList(usersPage);

        return UserPageResponse.from(usersPage, users);
    }

    // 사용자 목록을 응답 DTO 리스트로 매핑
    private List<AdminUserResponse> mapToResponseList(Page<User> usersPage) {
        return usersPage.getContent()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    // 특정 사용자 조회
    public AdminUserResponse getUserById(Long userId, User admin) {
        validateAdminAuthority(admin);
        User user = findByIdOrThrow(userId);
        return AdminUserResponse.from(user);
    }

    @Transactional
    public AdminUserResponse changeUserStatus(Long userId, AdminUserStatusUpdateRequest request, User admin) {
        validateAdminAuthority(admin);
        User user = findByIdOrThrow(userId);
        validateNotDuplicateStatus(user, request.status);

        validateStatusChangeRequest(request); // 상태별 검증 메서드로 분리

        user.changeStatus(request.status);
        userRepository.saveAndFlush(user);

        UserPenalty penalty = handleUserPenalty(user, request); // 패널티 로직 분리
        sendStatusChangeMailAsync(user, penalty); // 비동기 메일 전송 분리

        return AdminUserResponse.from(user);
    }

    private void validateStatusChangeRequest(AdminUserStatusUpdateRequest request) {
        switch (request.status) {
            case SUSPENDED -> validateSuspendRequest(request);
            case BANNED -> validateBanRequest(request);
            case ACTIVE -> request.clearReasonAndDate();
            default -> throw new ErrorException(ErrorCode.INVALID_STATUS);
        }
    }

    private void validateSuspendRequest(AdminUserStatusUpdateRequest request) {
        if (isBlank(request.reason)) {
            throw new ErrorException(ErrorCode.INVALID_SUSPEND_REASON);
        }
        if (request.suspendEndDate == null || request.suspendEndDate.isBefore(LocalDate.now())) {
            throw new ErrorException(ErrorCode.INVALID_SUSPEND_PERIOD);
        }
    }

    private void validateBanRequest(AdminUserStatusUpdateRequest request) {
        if (isBlank(request.reason)) {
            throw new ErrorException(ErrorCode.INVALID_SUSPEND_REASON);
        }
        if (request.suspendEndDate != null) {
            throw new ErrorException(ErrorCode.INVALID_BAN_PERIOD);
        }
    }

    // Penalty 기록 처리
    private UserPenalty handleUserPenalty(User user, AdminUserStatusUpdateRequest request) {
        if (request.status == AccountStatus.SUSPENDED || request.status == AccountStatus.BANNED) {
            UserPenalty penalty = UserPenalty.builder()
                    .user(user)
                    .reason(request.reason)
                    .startAt(LocalDateTime.now())
                    .endAt(request.suspendEndDate != null ? request.suspendEndDate.atStartOfDay() : null)
                    .released(false)
                    .appliedStatus(request.status)
                    .build();
            return userPenaltyRepository.saveAndFlush(penalty);
        }
        return null;
    }

    // 중복 상태 변경 방지
    private void validateNotDuplicateStatus(User user, AccountStatus newStatus) {
        if (user.getAccountStatus().equals(newStatus)) {
            throw new ErrorException(ErrorCode.DUPLICATE_STATUS);
        }
    }

    // 메일 전송 비동기 처리
    private void sendStatusChangeMailAsync(User user, UserPenalty penalty) {
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendStatusChangeMail(user, penalty);
            } catch (Exception e) {
                System.err.println("[메일 전송 실패] " + user.getEmail() + " - " + e.getMessage());
            }
        }, mailExecutor);
    }
}
