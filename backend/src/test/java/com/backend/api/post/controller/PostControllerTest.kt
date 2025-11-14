package com.backend.api.post.controller

import com.backend.api.post.dto.request.PostAddRequest
import com.backend.api.post.dto.request.PostUpdateRequest
import com.backend.domain.answer.repository.AnswerRepository
import com.backend.domain.post.entity.PinStatus
import com.backend.domain.post.entity.Post
import com.backend.domain.post.entity.PostCategoryType
import com.backend.domain.post.entity.PostStatus
import com.backend.domain.post.repository.PostRepository
import com.backend.domain.qna.repository.QnaRepository
import com.backend.domain.question.entity.Question
import com.backend.domain.question.repository.QuestionRepository
import com.backend.domain.ranking.repository.RankingRepository
import com.backend.domain.resume.repository.ResumeRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.domain.userQuestion.repository.UserQuestionRepository
import com.backend.global.Rq.Rq
import com.backend.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.transaction.Transactional
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var rq: Rq

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var questionRepository: QuestionRepository

    @Autowired
    private lateinit var answerRepository: AnswerRepository

    @Autowired
    private lateinit var qnaRepository: QnaRepository

    @Autowired
    private lateinit var rankingRepository: RankingRepository

    @Autowired
    private lateinit var userQuestionRepository: UserQuestionRepository

    @Autowired
    private lateinit var resumeRepository: ResumeRepository

    private lateinit var testUser: User
    private lateinit var otherUser: User
    private lateinit var savedPost: Post

    private val FIXED_DEADLINE: LocalDateTime = LocalDateTime.now().plusDays(7).withNano(0)

    @BeforeEach
    fun setUp() {
        objectMapper.registerModule(JavaTimeModule())

        userQuestionRepository.deleteAll()

        rankingRepository.deleteAll()
        resumeRepository.deleteAll()
        answerRepository.deleteAll()
        qnaRepository.deleteAll()

        postRepository.deleteAll()
        questionRepository.deleteAll()

        userRepository.deleteAll()


        testUser = User.builder()
            .email("test1@test.com").password("pw").name("작성자1").nickname("user1").age(20).role(Role.USER)
            .github("").build()
        userRepository.save(testUser)

        otherUser = User.builder()
            .email("other@test.com").password("pw").name("다른사람").nickname("other").age(20).role(Role.USER)
            .github("").build()
        userRepository.save(otherUser)

        val post = Post.builder()
            .title("기존 제목")
            .introduction("기존 한줄 소개입니다. 10자 이상.")
            .content("기존 프로젝트 모집글 내용입니다. 10자 이상.")
            .users(testUser)
            .deadline(FIXED_DEADLINE)
            .status(PostStatus.ING)
            .pinStatus(PinStatus.NOT_PINNED)
            .recruitCount(4)
            .postCategoryType(PostCategoryType.PROJECT)
            .build()
        savedPost = postRepository.save(post)

        val question = Question.builder()
            .title("테스트 질문 제목")
            .content("테스트 질문 내용입니다.")
            .author(testUser)
            .build()
        questionRepository.save(question)
    }

    @Nested
    @DisplayName("게시글 생성 API")
    inner class CreatePostApiTest {

        @Test
        @DisplayName("게시글 작성 성공")
        fun success() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(testUser)

            val request = PostAddRequest(
                "새로운 게시물",
                "새로운 프로젝트 모집글 내용입니다. 10자 이상.",
                "새로운 한 줄 소개입니다. 10자 이상.",
                FIXED_DEADLINE,
                PostStatus.ING,
                PinStatus.NOT_PINNED,
                5,
                PostCategoryType.PROJECT
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.introduction").value("새로운 한 줄 소개입니다. 10자 이상."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("새로운 게시물"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.recruitCount").value(5))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자(로그인 X)")
        fun fail1() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(null)

            val request = PostAddRequest(
                "첫번째 게시물",
                "함께 팀 프로젝트를 진행할 백엔드 개발자 구합니다. 10자 이상.",
                "열정적인 팀원을 찾습니다. 10자 이상.",
                FIXED_DEADLINE,
                PostStatus.ING,
                PinStatus.NOT_PINNED,
                4,
                PostCategoryType.PROJECT
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("로그인된 사용자가 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 제목 누락")
        fun fail2() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(testUser)

            val request = PostAddRequest(
                "",
                "내용은 10자 이상으로 충분합니다.",
                "한 줄 소개도 10자 이상으로 충분합니다.",
                FIXED_DEADLINE,
                PostStatus.ING,
                PinStatus.NOT_PINNED,
                4,
                PostCategoryType.PROJECT
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("제목은 필수입니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 내용 누락")
        fun fail3() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(testUser)


            val request = PostAddRequest(
                "제목은 있습니다.",
                "",
                "한 줄 소개는 10자 이상으로 충분합니다.",
                FIXED_DEADLINE,
                PostStatus.ING,
                PinStatus.NOT_PINNED,
                4,
                PostCategoryType.PROJECT
            )


            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists())
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 마감일이 과거 날짜")
        fun fail_deadline_in_past() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(testUser)
            val request = PostAddRequest(
                "마감일이 과거인 게시물",
                "내용은 충분히 깁니다. 10자 이상입니다.",
                "한 줄 소개도 충분히 깁니다. 10자 이상입니다.",
                LocalDateTime.now().minusDays(1),  // 과거 날짜로 설정
                PostStatus.ING,
                PinStatus.NOT_PINNED,
                4,
                PostCategoryType.PROJECT
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.INVALID_DEADLINE.getMessage()))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("게시글 수정 API")
    inner class UpdatePostApiTest {

        @Test
        @DisplayName("게시글 수정 성공")
        fun success() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(testUser)
            val request = PostUpdateRequest(
                "수정된 제목",
                "수정된 한 줄 소개입니다. 10자 이상.",
                "수정된 내용입니다. 10자 이상.",
                FIXED_DEADLINE,
                PostStatus.CLOSED,
                PinStatus.PINNED,
                10,
                PostCategoryType.PROJECT
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/posts/{postId}", savedPost.id)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.introduction").value("수정된 한 줄 소개입니다. 10자 이상."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("수정된 내용입니다. 10자 이상."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value(PostStatus.CLOSED.name))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.pinStatus").value(PinStatus.PINNED.name))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.recruitCount").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.categoryType").value("PROJECT"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        fun fail_post_not_found() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(testUser)
            val request = PostUpdateRequest(
                "수정된 제목",
                "수정된 한 줄 소개입니다. 10자 이상.",
                "수정된 내용입니다. 10자 이상.",
                FIXED_DEADLINE,
                PostStatus.CLOSED,
                PinStatus.PINNED,
                10,
                PostCategoryType.PROJECT
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/posts/{postId}", 999L)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("존재하지 않는 게시글입니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 작성자가 아님")
        fun fail_not_owner() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(otherUser)
            val request = PostUpdateRequest(
                "수정된 제목",
                "수정된 한 줄 소개입니다. 10자 이상.",
                "수정된 내용입니다. 10자 이상.",
                FIXED_DEADLINE,
                PostStatus.CLOSED,
                PinStatus.PINNED,
                10,
                PostCategoryType.PROJECT
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/posts/{postId}", savedPost.id)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("접근 권한이 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 제목 누락")
        fun fail_title_blank() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(testUser)


            val request = PostUpdateRequest(
                "",
                "수정된 한 줄 소개입니다. 10자 이상.",
                "수정된 내용입니다. 10자 이상.",
                FIXED_DEADLINE,
                PostStatus.CLOSED,
                PinStatus.PINNED,
                10,
                PostCategoryType.PROJECT
            )


            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/posts/{postId}", savedPost.id)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists()) // 제목 관련 에러 메시지 중 하나가 나옴
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 마감일을 과거로 수정")
        fun fail_update_deadline_to_past() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(testUser)
            val request = PostUpdateRequest(
                "수정된 제목",
                "수정된 한 줄 소개입니다. 10자 이상입니다.",
                "수정된 내용입니다. 10자 이상입니다.",
                LocalDateTime.now().minusDays(1),  // 과거 날짜로 설정
                PostStatus.ING,
                PinStatus.NOT_PINNED,
                10,
                PostCategoryType.PROJECT
            )

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/posts/{postId}", savedPost.id)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.INVALID_DEADLINE.getMessage()))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("게시글 삭제 API")
    inner class DeletePostApiTest {

        @Test
        @DisplayName("게시글 삭제 성공")
        fun success() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(testUser)
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/{postId}", savedPost.id)
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("게시글 삭제가 완료되었습니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        fun fail_post_not_found() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(testUser)
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/{postId}", 999L)
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("존재하지 않는 게시글입니다."))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 작성자가 아님")
        fun fail_not_owner() {
            // given
            Mockito.`when`(rq.getUser()).thenReturn(otherUser)
            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/{postId}", savedPost.id)
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("접근 권한이 없습니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("게시글 단건 조회 API")
    inner class GetPostApiTest {

        @Test
        @DisplayName("게시글 조회 성공")
        fun success() {
            // given
            val postId = savedPost.id

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/posts/{postId}", postId)
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.message").value("${postId}번 게시글을 성공적으로 조회했습니다.")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(postId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("기존 제목"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.introduction").value("기존 한줄 소개입니다. 10자 이상."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("기존 프로젝트 모집글 내용입니다. 10자 이상."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.recruitCount").value(4))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        fun fail_post_not_found() {
            // given
            val nonExistentPostId = 999L

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/posts/{postId}", nonExistentPostId)
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("존재하지 않는 게시글입니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("게시글 다건 조회 API")
    inner class GetAllPostsApiTest {

        @Test
        @DisplayName("게시글 다건 조회 성공")
        fun success() {
            val postAuthor = User.builder()
                .email("post_author@test.com").password("pw").name("게시글작성자").nickname("post_author").age(25)
                .role(Role.USER)
                .github("").build()
            userRepository.save(postAuthor)

            val anotherPost = Post.builder()
                .title("두 번째 게시글")
                .introduction("두 번째 한줄 소개입니다. 10자 이상.")
                .content("두 번째 내용입니다. 10자 이상.")
                .users(postAuthor)
                .deadline(FIXED_DEADLINE)
                .status(PostStatus.ING)
                .pinStatus(PinStatus.NOT_PINNED)
                .recruitCount(2)
                .postCategoryType(PostCategoryType.PROJECT)
                .build()
            postRepository.save(anotherPost)


            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/posts")
                    .queryParam("page", "1")
                    .accept(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("전체 게시글 조회 성공"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts.length()").value(2))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.posts[0].title").value("두 번째 게시글")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts[1].title").value("기존 제목"))
                .andDo(MockMvcResultHandlers.print())
        }
    }

    @Nested
    @DisplayName("카테고리별 게시글 조회 API")
    inner class GetPostsByCategoryApiTest {

        @Test
        @DisplayName("카테고리별 게시글 조회 성공 - PROJECT")
        fun success_project() {
            // given
            val post1 = Post.builder()
                .title("프로젝트 게시글 1")
                .introduction("프로젝트 소개 1")
                .content("프로젝트 게시글의 내용입니다.")
                .deadline(FIXED_DEADLINE)
                .status(PostStatus.ING)
                .pinStatus(PinStatus.NOT_PINNED)
                .recruitCount(3)
                .users(testUser)
                .postCategoryType(PostCategoryType.PROJECT)
                .build()
            postRepository.save(post1)

            val post2 = Post.builder()
                .title("프로젝트 게시글 2")
                .introduction("프로젝트 소개 2")
                .content("프로젝트 게시글의 내용입니다.")
                .deadline(FIXED_DEADLINE)
                .status(PostStatus.ING)
                .pinStatus(PinStatus.NOT_PINNED)
                .recruitCount(5)
                .users(testUser)
                .postCategoryType(PostCategoryType.PROJECT)
                .build()
            postRepository.save(post2)

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/posts/category/{categoryType}", "PROJECT")
                    .queryParam("page", "1")
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("카테고리별 게시글 조회 성공"))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.posts.length()").value(3)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts[0].categoryType").value("PROJECT"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("카테고리별 게시글 조회 성공 - STUDY")
        fun success_study() {
            // given
            val studyPost = Post.builder()
                .title("스터디 게시글 1")
                .introduction("스터디 소개 1")
                .content("스터디 게시글의 내용입니다.")
                .deadline(FIXED_DEADLINE)
                .status(PostStatus.ING)
                .pinStatus(PinStatus.NOT_PINNED)
                .recruitCount(2)
                .users(testUser)
                .postCategoryType(PostCategoryType.STUDY)
                .build()
            postRepository.save(studyPost)

            // when
            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/posts/category/{categoryType}", "STUDY")
                    .queryParam("page", "1")
                    .accept(MediaType.APPLICATION_JSON)
            )

            // then
            resultActions
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("카테고리별 게시글 조회 성공"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts[0].categoryType").value("STUDY"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.posts[0].title").value("스터디 게시글 1"))
                .andDo(MockMvcResultHandlers.print())
        }

        @Test
        @DisplayName("실패 - 해당 카테고리에 게시글이 없음")
        fun fail_empty_category() {
            // when

            val resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/posts/category/{categoryType}", "STUDY")
                    .queryParam("page", "1")
                    .accept(MediaType.APPLICATION_JSON)
            )

            resultActions
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("존재하지 않는 게시글입니다."))
                .andDo(MockMvcResultHandlers.print())
        }
    }
}