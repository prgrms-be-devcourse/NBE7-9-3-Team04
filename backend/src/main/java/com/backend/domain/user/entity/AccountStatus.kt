package com.backend.domain.user.entity

enum class AccountStatus {
    ACTIVE,       // 활성 상태(기본값)
    SUSPENDED,    // 일시정지 (약관 위반 등)
    DEACTIVATED,  // 비활성화 (탈퇴 상태)
    BANNED        // 영구정지 (심각한 위반 등)
}
