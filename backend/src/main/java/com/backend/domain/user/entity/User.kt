package com.backend.domain.user.entity

import com.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(

    @Column(length = 100, nullable = false, unique = true)
    var email: String,

    @Column(length = 255, nullable = false)
    var password: String,

    @Column(length = 50, nullable = false)
    var name: String,

    @Column(length = 50, nullable = false)
    var nickname: String,

    @Column(nullable = false)
    var age: Int = 0,

    @Column(length = 255, nullable = false)
    var github: String,

    @Column(length = 255, nullable = true)
    var image: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var accountStatus: AccountStatus = AccountStatus.ACTIVE, // 기본값 ACTIVE

    @Column(nullable = false)
    var aiQuestionUsedCount: Int = 0, // AI 질문 사용 횟수
//
//    @OneToOne(
//        mappedBy = "user",
//        cascade = [CascadeType.ALL],
//        orphanRemoval = true,
//        fetch = FetchType.LAZY
//    )
//    var subscription: Subscription? = null

) : BaseEntity() {

    constructor() : this(
        email = "",
        password = "",
        name = "",
        nickname = "",
        age = 0,
        github = "",
        image = null,
        role = Role.USER,
        accountStatus = AccountStatus.ACTIVE,
        aiQuestionUsedCount = 0,
        //subscription = null
    )
//
//    fun assignSubscription(subscription: Subscription) {
//        this.subscription = subscription
//    }

    fun updateUser(
        email: String, password: String, name: String,
        nickname: String, age: Int, github: String, image: String?
    ) {
        this.email = email
        this.password = password
        this.name = name
        this.nickname = nickname
        this.age = age
        this.github = github
        this.image = image
    }

    fun changeStatus(newStatus: AccountStatus) {
        if (this.accountStatus != newStatus) {
            this.accountStatus = newStatus
        }
    }

    fun validateActiveStatus(): Boolean {
        return this.accountStatus == AccountStatus.ACTIVE
    }

    fun validateLoginAvaliable(): Boolean {
        return this.accountStatus == AccountStatus.ACTIVE ||
                this.accountStatus == AccountStatus.SUSPENDED
    }

//    fun isPremium(): Boolean = this.subscription?.isValid() == true
//
//
//    fun getAiQuestionLimit(): Int =
//        subscription?.questionLimit ?: 5 // 구독 정보가 없는 예외적인 경우, 기본값 5를 반환합니다.

    fun incrementAiQuestionUsedCount() {
        this.aiQuestionUsedCount++
    }

    fun changePassword(encodedPassword: String) {
        this.password = encodedPassword
    }
}
