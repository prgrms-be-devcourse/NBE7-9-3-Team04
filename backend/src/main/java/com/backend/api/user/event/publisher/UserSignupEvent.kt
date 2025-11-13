package com.backend.api.user.event.publisher

import com.backend.domain.user.entity.User

data class UserSignupEvent(
    val user: User

)
