package com.backend.domain.user.entity;

import com.backend.domain.subscription.entity.Subscription;
import com.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private int age;

    @Column(length = 255, nullable = false)
    private String github;

    @Column(length = 255, nullable = true)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE; // 기본값 ACTIVE

    @Column(nullable = false)
    @Builder.Default
    private int aiQuestionUsedCount = 0; // AI 질문 사용 횟수


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Subscription subscription;


    public void assignSubscription(Subscription subscription) {
        this.subscription = subscription;
    }


    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public int getAge() {
        return age;
    }

    public String getGithub() {
        return github;
    }

    public String getImage() {
        return image;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public int getAiQuestionUsedCount() {
        return aiQuestionUsedCount;
    }

    public Subscription getSubscription() {
        return subscription;
    }


    public void updateUser(String email, String password, String name,
                           String nickname, int age, String github, String image) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.age = age;
        this.github = github;
        this.image = image;
    }

    public void changeStatus(AccountStatus newStatus) {
        if (newStatus != null && !this.accountStatus.equals(newStatus)) {
            this.accountStatus = newStatus;
        }
    }

    public boolean validateActiveStatus() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }

    public boolean validateLoginAvaliable() {
        return this.accountStatus == AccountStatus.ACTIVE || this.accountStatus == AccountStatus.SUSPENDED;
    }

    public boolean isPremium() {
        return this.subscription != null && this.subscription.isValid();
    }

    public int getAiQuestionLimit() {
        if (isPremium()) {
            return this.subscription.getQuestionLimit();
        }
        return 5; // 구독 정보가 없는 예외적인 경우, 기본값 5를 반환합니다.
    }

    public void incrementAiQuestionUsedCount() {
        this.aiQuestionUsedCount++;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
        subscription.setUser(this);
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
