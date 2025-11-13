package com.backend.api.user.event.publisher

import com.backend.domain.user.entity.User
import com.backend.domain.userPenalty.entity.UserPenalty

data class UserStatusChangeEvent(
    val user: User,
    val penalty: UserPenalty?
)
