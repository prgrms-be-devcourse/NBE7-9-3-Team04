package com.backend.domain.user.repository

import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByOauthId(oauthId: String): User?
    fun existsByRole(role: Role): Boolean

    //관리자 제외 조회용 메서드 추가
    fun findAllByRoleNot(role: Role, pageable: Pageable): Page<User>

    //엘라스틱 서치와 비교용 MysQL 검색 메서드 추가
    @Query("select u from User u where u.name like :name or u.nickname like :nickname")
    fun searchByNameOrNickname(
        @Param("name") name: String,
        @Param("nickname") nickname: String,
        pageable: Pageable
    ): Page<User>
}
