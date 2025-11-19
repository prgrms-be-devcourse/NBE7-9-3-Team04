package com.backend.api.comment.controller

import com.backend.api.global.JwtTest
import com.backend.domain.comment.entity.Comment
import com.backend.domain.comment.repository.CommentRepository
import com.backend.domain.post.entity.PinStatus
import com.backend.domain.post.entity.Post
import com.backend.domain.post.entity.PostCategoryType
import com.backend.domain.post.entity.PostStatus
import com.backend.domain.post.repository.PostRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.global.security.CustomUserDetails
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.function.Supplier


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CommentControllerTest(
    private val mvc: MockMvc,
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
): JwtTest() {

    @BeforeEach
    @Transactional
    fun setUp() {
        val generalUser = User(
            "general@user.com",
            "asdf1234!",
            "홍길동",
            "gildong",
            20,
            "abc123",
            null,
            Role.USER
        )

        val generalUser2 = User(
            "general2@user.com",
            "asdf1234!",
            "홍길똥",
            "gilddong",
            25,
            "abc1233",
            null,
            Role.USER
        )

        userRepository.save(generalUser)
        userRepository.save(generalUser2)

        val post1 = Post(
            "제목",
            "소개",
            "내용12321321321321",
            LocalDateTime.now().plusDays(7),
            PostStatus.ING,
            PinStatus.NOT_PINNED,
            5,
            userRepository.findById(1L).orElseThrow(),
            PostCategoryType.PROJECT
        )
        postRepository.save(post1)

        val comment1 = Comment(
            "1번 댓글",
            post1,
            userRepository.findById(1L).orElseThrow()
        )

        val comment2 = Comment(
            "2번 댓글",
            post1,
            userRepository.findById(2L).orElseThrow()
        )

        val comment3 = Comment(
            "3번 댓글",
            post1,
            userRepository.findById(1L).orElseThrow()
        )

        commentRepository.save<Comment?>(comment1)
        commentRepository.save<Comment?>(comment2)
        commentRepository.save<Comment?>(comment3)
    }

    @BeforeEach
    fun setupAuth() {
        val user = userRepository.findById(1L).get()
        val userDetails = CustomUserDetails(user)

        val auth: Authentication = UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        )

        SecurityContextHolder.getContext().setAuthentication(auth)
    }

    @Nested
    @DisplayName("댓글 생성 테스트")
    inner class CreateCommentTest {

        @Test
        @DisplayName("댓글 생성 - 1번 게시글에 생성")
        @Throws(Exception::class)
        fun t1() {
            val targetPostId: Long = 1 // 동적으로 생성된 게시글 ID 사용
            val content = "새로운 댓글"

            // DB에 댓글 생성 전 개수
            val initialCommentCount = commentRepository.count()

            val resultActions = mvc
                .perform(
                    post("/api/v1/posts/${targetPostId}/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                                        {
                                            "content": "$content"
                                        }
                                        
                                        """.trimIndent()
                        )
                )
                .andDo(MockMvcResultHandlers.print())

            // DB에 댓글 생성 후 개수 확인
            val newComments: MutableList<Comment?> = commentRepository.findAll()
            Assertions.assertThat(newComments.size).isEqualTo(initialCommentCount + 1)

            // 생성된 댓글 정보 동적 확인
            val createdComment = newComments.stream()
                .filter { c: Comment? -> c!!.content == content }
                .filter { c: Comment? -> c!!.post.id == (targetPostId) }
                .findFirst()
                .orElseThrow(Supplier { AssertionError("새로 생성된 댓글을 찾을 수 없습니다.") })

            val createdCommentId = createdComment!!.id

            resultActions
                .andExpect(handler().handlerType(CommentController::class.java))
                .andExpect(handler().methodName("createComment"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(
                    jsonPath("$.message").value("${createdCommentId}번 댓글이 생성되었습니다.")
                )
                .andExpect(jsonPath("$.data.id").value(createdCommentId))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.authorId").value(createdComment.author.id))
                .andExpect(
                    jsonPath("$.data.authorNickName").value(createdComment.author.nickname)
                )
                .andExpect(jsonPath("$.data.postId").value(targetPostId))
        }

        @Test
        @DisplayName("댓글 생성 실패 - 내용이 비어 있을 때")
        @Throws(Exception::class)
        fun t1_2() {
            val targetPostId: Long = 1
            val resultActions = mvc
                .perform(
                    post("/api/v1/posts/${targetPostId}/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                                        {
                                            "content": null
                                        }
                                        
                                        """.trimIndent()
                        )
                )
                .andDo(MockMvcResultHandlers.print())
            resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("댓글 내용을 입력해주세요."))
                .andExpect(jsonPath("$.data").doesNotExist())
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 댓글 생성 시도")
        fun fail2() {
            val nonExistentPostId = 9999L
            val comment = "새로운 댓글 내용입니다."

            mvc.perform(
                post("/api/v1/posts/$nonExistentPostId/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "content": "$comment"
                        }
                    """.trimIndent()
                    )
            ).andDo(MockMvcResultHandlers.print())
                .andExpect(handler().handlerType(CommentController::class.java))
                .andExpect(handler().methodName("createComment"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."))
        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    inner class UpdateCommentTest {

        @Test
        @DisplayName("댓글 수정 - 자신의 댓글 수정")
        @Throws(Exception::class)
        fun t2() {
            val targetPostId: Long = 7 // 동적으로 생성된 게시글 ID 사용
            val targetCommentId: Long = 1 // 동적으로 생성된 댓글 ID 사용
            val content = "수정한 댓글" // 수정할 내용
            val expectedAuthorId: Long = 1 // 예상 작성자 ID
            val expectedAuthorNickname = "user1" // 예상 작성자 닉네임

            // 초기 modifyDate 값 캡처 (수정되기 전의 시간)
            val initialModifyDate = commentRepository.findById(targetCommentId)
                .orElseThrow(Supplier { java.lang.AssertionError("댓글을 찾을 수 없습니다.") })!!
                .modifyDate

            // modifiedDate가 초기값보다 확실히 이후가 되도록 보장
            Thread.sleep(100)

            val resultActions = mvc
                .perform(
                    patch("/api/v1/posts/${targetPostId}/comments/${targetCommentId}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                                        {
                                            "content": "$content"
                                        }
                                        
                                        """.trimIndent()
                        )
                )
                .andDo(MockMvcResultHandlers.print())

            // [추가] 응답 본문을 통해 댓글 ID, 게시글 ID, 수정된 내용이 올바른지 검증
            resultActions
                .andExpect(handler().handlerType(CommentController::class.java))
                .andExpect(handler().methodName("updateComment")) // PATCH에 맞는 메서드 이름
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("${targetCommentId}번 댓글이 수정되었습니다."))
                .andExpect(jsonPath("$.data.id").value(targetCommentId))
                .andExpect(jsonPath("$.data.createDate").exists())
                .andExpect(jsonPath("$.data.modifyDate").exists())
                .andExpect(jsonPath("$.data.content").value(content))
                .andExpect(jsonPath("$.data.authorId").value(expectedAuthorId))
                .andExpect(jsonPath("$.data.authorNickName").value(expectedAuthorNickname))
                .andExpect(jsonPath("$.data.postId").value(targetPostId))

            commentRepository.flush()

            // DB 확인: 실제로 수정된 내용이 반영되었는지 최종 검증
            val optionalComment: Optional<Comment?> = commentRepository.findById(targetCommentId)
            Assertions.assertThat<Comment?>(optionalComment).isPresent()
            val updatedComment = optionalComment.get()
            Assertions.assertThat(updatedComment.content).isEqualTo(content) // DB에서 변경내용 확인
            Assertions.assertThat(updatedComment.post.id).isEqualTo(targetPostId) // DB에서 Post ID 확인
            Assertions.assertThat(updatedComment.author.id).isEqualTo(expectedAuthorId) // DB에서 Author ID 확인
            Assertions.assertThat(updatedComment.modifyDate).isAfter(updatedComment.createDate) // 수정일이 생성일 이후인지 확인
            Assertions.assertThat(updatedComment.modifyDate).isAfter(initialModifyDate) // 수정 날짜가 초기 날짜보다 이후인지 확인
        }

        @Test
        @DisplayName("댓글 수정 - 다른 작성자의 댓글 수정 시도")
        @Throws(Exception::class)
        fun t3() {
            val targetPostId: Long = 14
            val targetCommentId: Long = 3
            val content = "수정한 댓글"

            val resultActions = mvc
                .perform(
                    patch("/api/v1/posts/${targetPostId}/comments/${targetCommentId}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                                        {
                                            "content": "$content"
                                        }
                                        
                                        """.trimIndent()
                        )
                )
                .andDo(MockMvcResultHandlers.print())

            resultActions
                .andExpect(handler().handlerType(CommentController::class.java))
                .andExpect(handler().methodName("updateComment"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("해당 댓글에 대한 권한이 없는 사용자입니다."))
        }

        @Test
        @DisplayName("댓글 수정 - 없는 댓글에 대한 수정 요청")
        fun fail2() {
            val nonExistentCommentId = 9999L
            val content = "수정된 댓글 내용입니다."

            mvc.perform(
                patch("/api/v1/posts/1/comments/$nonExistentCommentId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                            "content": "$content"
                        }
                    """.trimIndent()
                    )
            ).andDo(MockMvcResultHandlers.print())
                .andExpect(handler().handlerType(CommentController::class.java))
                .andExpect(handler().methodName("updateComment"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 댓글입니다."))
        }
    }

    @Nested
    @DisplayName("댓글 삭제 테스트")
    inner class DeleteCommentTest {

        @Test
        @DisplayName("댓글 삭제 - 1번 글의 1번 댓글 삭제")
        @Throws(Exception::class)
        fun t4() {
            val targetPostId: Long = 1
            val targetCommentId: Long = 1

            val resultActions = mvc
                .perform(
                    delete("/api/v1/posts/${targetPostId}/comments/${targetCommentId}")
                )
                .andDo(MockMvcResultHandlers.print())

            // 필수 검증
            resultActions
                .andExpect(handler().handlerType(CommentController::class.java))
                .andExpect(handler().methodName("deleteComment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("${targetCommentId}번 댓글이 삭제되었습니다."))

            // 선택적 검증
            val comment = commentRepository.findById(targetCommentId).orElse(null)
            Assertions.assertThat<Comment?>(comment).isNull()
        }

        @Test
        @DisplayName("댓글 삭제 - 다른 사용자의 댓글 삭제 시도")
        @Throws(Exception::class)
        fun t5() {
            val targetPostId: Long = 14
            val targetCommentId: Long = 3

            val resultActions = mvc
                .perform(
                    delete("/api/v1/posts/${targetPostId}/comments/${targetCommentId}")
                )
                .andDo(MockMvcResultHandlers.print())

            // 필수 검증
            resultActions
                .andExpect(handler().handlerType(CommentController::class.java))
                .andExpect(handler().methodName("deleteComment"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("해당 댓글에 대한 권한이 없는 사용자입니다."))
        }
    }

    @Nested
    @DisplayName("댓글 조회 테스트")
    inner class ReadCommentTest {

        @Test
        @DisplayName("댓글 조회")
        @Throws(Exception::class)
        fun t6() {
            val targetPostId: Long = 16

            val resultActions = mvc
                .perform(
                    get("/api/v1/posts/${targetPostId}/comments")
                )
                .andDo(MockMvcResultHandlers.print())

            resultActions
                .andExpect(handler().handlerType(CommentController::class.java))
                .andExpect(handler().methodName("readComments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(
                    jsonPath("$.message").value("${targetPostId}번 게시글의 댓글 목록 조회 성공")
                )

            resultActions
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(
                    MockMvcResultMatchers.jsonPath(
                        "$.data.comments[*].id",
                        Matchers.containsInRelativeOrder(5, 6)
                    )
                )
                .andExpect(jsonPath("$.data.comments[1].id").value(6))
                .andExpect(jsonPath("$.data.comments[1].createDate").exists())
                .andExpect(jsonPath("$.data.comments[1].modifyDate").exists())
                .andExpect(jsonPath("$.data.comments[1].content").value("혹시 백엔드 포지션도 모집하시나요?"))
                .andExpect(jsonPath("$.data.comments[1].authorId").value(4))
                .andExpect(jsonPath("$.data.comments[1].authorNickName").value("user4"))
                .andExpect(jsonPath("$.data.comments[1].postId").value(targetPostId))
        }

        @Test
        @DisplayName("댓글 조회 - 존재하지 않는 게시글의 댓글 조회 시도")
        fun fail3() {
            val nonExistentPostId = 9999L

            mvc.perform(
                get("/api/v1/posts/$nonExistentPostId/comments")
            ).andDo(MockMvcResultHandlers.print())
                .andExpect(handler().handlerType(CommentController::class.java))
                .andExpect(handler().methodName("readComments"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."))
        }
    }
}
