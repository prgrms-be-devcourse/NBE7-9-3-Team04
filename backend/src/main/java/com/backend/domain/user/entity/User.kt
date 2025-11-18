package com.backend.domain.user.entity

import com.backend.domain.subscription.entity.Subscription
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

    @OneToOne(
        mappedBy = "user",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var subscription: Subscription? = null,

    @Column(unique = true)
    var oauthId: String? = null

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
        subscription = null,
        oauthId =null
    )

    fun assignSubscription(subscription: Subscription) {
        this.subscription = subscription
    }

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

    fun isPremium(): Boolean = this.subscription?.isValid() == true


    fun getAiQuestionLimit(): Int =
        subscription?.questionLimit ?: 5 // 구독 정보가 없는 예외적인 경우, 기본값 5를 반환합니다.

    fun incrementAiQuestionUsedCount() {
        this.aiQuestionUsedCount++
    }

    fun changePassword(encodedPassword: String) {
        this.password = encodedPassword
    }

    //TODO 임시 빌더 제거
    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var email: String = ""
        private var password: String = ""
        private var name: String = ""
        private var nickname: String = ""
        private var age: Int = 0
        private var github: String = ""
        private var image: String? = null
        private var role: Role = Role.USER
        private var accountStatus: AccountStatus = AccountStatus.ACTIVE
        private var aiQuestionUsedCount: Int = 0
        private var subscription: Subscription? = null

        fun email(email: String) = apply { this.email = email }
        fun password(password: String) = apply { this.password = password }
        fun name(name: String) = apply { this.name = name }
        fun nickname(nickname: String) = apply { this.nickname = nickname }
        fun age(age: Int) = apply { this.age = age }
        fun github(github: String) = apply { this.github = github }
        fun image(image: String?) = apply { this.image = image }
        fun role(role: Role) = apply { this.role = role }
        fun accountStatus(status: AccountStatus) = apply { this.accountStatus = status }
        fun aiQuestionUsedCount(count: Int) = apply { this.aiQuestionUsedCount = count }
        fun subscription(subscription: Subscription?) = apply { this.subscription = subscription }

        fun build(): User = User(
            email,
            password,
            name,
            nickname,
            age,
            github,
            image,
            role,
            accountStatus,
            aiQuestionUsedCount,
            subscription
        )
    }
}
