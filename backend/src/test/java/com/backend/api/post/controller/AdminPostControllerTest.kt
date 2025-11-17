package com.backend.api.post.controller

import com.backend.api.post.dto.request.AdminPostPinRequest
import com.backend.api.post.dto.request.AdminPostStatusRequest
import com.backend.domain.post.entity.PinStatus
import com.backend.domain.post.entity.Post
import com.backend.domain.post.entity.PostCategoryType
import com.backend.domain.post.entity.PostStatus
import com.backend.domain.post.repository.PostRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AdminPostControllerTest(
    private val mockMvc: MockMvc,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper
) {

    @MockBean
    private lateinit var rq: Rq

    private lateinit var admin: User
    private lateinit var user: User
    private lateinit var post: Post

    @BeforeEach
    fun setUp() {
        admin = userRepository.save(
            User.builder()
                .email("admin@test.com")
                .password("admin1234!")
                .name("관리자")
                .nickname("admin")
                .age(30)
                .github("github.com/admin")
                .role(Role.ADMIN)
                .build()
        )

        user = userRepository.save(
            User.builder()
                .email("user@test.com")
                .password("user1234!")
                .name("일반유저")
                .nickname("user")
                .age(25)
                .github("github.com/user")
                .role(Role.USER)
                .build()
        )

        post = Post.builder()
            .title("관리자용 테스트 게시글")
            .introduction("관리자 테스트용 소개입니다.")
            .content("관리자 테스트용 내용입니다. 10자 이상입니다.")
            .deadline(LocalDateTime.now().plusDays(7))
            .status(PostStatus.ING)
            .pinStatus(PinStatus.NOT_PINNED)
            .recruitCount(3)
            .postCategoryType(PostCategoryType.PROJECT)
            .users(admin)
            .build()

        post = postRepository.save(post)

        Mockito.`when`(rq.getUser()).thenReturn(admin)
    }

    @Nested
    @DisplayName("관리자 전체 게시글 조회 API")
    inner class t1 {
        @Test
        @DisplayName("전체 게시글 조회 성공")
        fun success() {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/posts"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("전체 게시글 조회 성공"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts").isArray())
                .andExpect(
                    MockMvcResultMatchers.jsonPath(
                        "$.data.posts[*].title",
                        Matchers.hasItem("관리자용 테스트 게시글")
                    )
                )
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 비로그인 상태")
        fun fail1() {
            Mockito.`when`(rq.getUser()).thenReturn(null)

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/posts"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("로그인된 사용자가 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 관리자 아닌 경우")
        fun fail2() {
            Mockito.`when`(rq.getUser()).thenReturn(user)

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/posts"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("접근 권한이 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("관리자 게시글 단건 조회 API")
    inner class t2 {
        @Test
        @DisplayName("게시글 단건 조회 성공")
        fun success() {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/posts/{postId}", post.id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 단건 조회 성공"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("관리자용 테스트 게시글"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        fun fail1() {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/posts/{postId}", 99999L))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("존재하지 않는 게시글입니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("관리자 게시글 삭제 API")
    inner class t3 {
        @Test
        @DisplayName("게시글 삭제 성공")
        fun success() {
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/admin/posts/{postId}", post.id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 삭제 성공"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 관리자 아닌 경우")
        fun fail1() {
            Mockito.`when`(rq.getUser()).thenReturn(user)

            mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/admin/posts/{postId}", post.id))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("접근 권한이 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("관리자 게시글 핀 상태 변경 API")
    inner class t4 {
        @Test
        @DisplayName("게시글 핀 상태 변경 성공")
        fun success() {
            val request = AdminPostPinRequest(PinStatus.PINNED)

            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/v1/admin/posts/{postId}/pin", post.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 고정 상태 변경 성공"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.pinStatus").value("PINNED"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 바디")
        fun fail1() {
            val invalidBody = "{}"

            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/v1/admin/posts/{postId}/pin", post.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidBody)
            )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("상단 고정 상태는 필수입니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("관리자 게시글 진행 상태 변경 API")
    inner class t5 {
        @Test
        @DisplayName("게시글 진행 상태 변경 성공")
        fun success() {
            val postId = post.id
            val request = AdminPostStatusRequest(PostStatus.CLOSED)

            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/v1/admin/posts/{postId}/status", postId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 진행 상태가 변경 성공"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("CLOSED"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 잘못된 요청 바디")
        fun fail1() {
            val invalidBody = "{}"

            mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/v1/admin/posts/{postId}/status", post.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidBody)
            )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 상태는 필수입니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }
}