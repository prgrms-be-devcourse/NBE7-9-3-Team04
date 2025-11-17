package com.backend.api.qna.controller

import com.backend.api.global.JwtTest
import com.backend.api.qna.dto.request.QnaAddRequest
import com.backend.api.qna.dto.request.QnaUpdateRequest
import com.backend.domain.qna.entity.Qna
import com.backend.domain.qna.entity.QnaCategoryType
import com.backend.domain.qna.repository.QnaRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class QnaControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val qnaRepository: QnaRepository,
    override var userRepository: UserRepository
) : JwtTest() {

    @MockBean
    private lateinit var rq: Rq

    private lateinit var testUser: User
    private lateinit var savedQna: Qna

    @BeforeEach
    fun setUp() {
        testUser = userRepository.save(
            User(
                name = "테스터",
                email = "test@qna.com",
                nickname = "tester",
                github = "https://github.com/tester",
                password = "1234",
                role = Role.USER
            )
        )

        Mockito.`when`(rq.getUser()).thenReturn(testUser)

        savedQna = qnaRepository.save(
            Qna(
                title = "기존 제목",
                content = "기본 내용",
                author = testUser,
                categoryType = QnaCategoryType.ACCOUNT
            )
        )
    }

    @Nested
    @DisplayName("QnA 조회 API")
    inner class GetQnaTest {

        @Test
        @DisplayName("QnA 전체 조회 성공")
        fun success_all() {
            mockMvc.perform(
                get("/api/v1/qna")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Qna 목록 조회 성공"))
                .andExpect(jsonPath("$.data.qna[0].title").value("기존 제목"))
                .andExpect(jsonPath("$.data.qna[0].categoryType").value("ACCOUNT"))
                .andDo(print())
        }

        @Test
        @DisplayName("QnA 단건 조회 성공")
        fun success_single() {
            mockMvc.perform(
                get("/api/v1/qna/{qnaId}", savedQna.id)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("${savedQna.id}번 Qna 조회 성공"))
                .andExpect(jsonPath("$.data.title").value("기존 제목"))
                .andExpect(jsonPath("$.data.categoryType").value("ACCOUNT"))
                .andDo(print())
        }

        @Test
        @DisplayName("QnA 단건 조회 실패 - 존재하지 않는 ID")
        fun fail_notFound() {
            mockMvc.perform(
                get("/api/v1/qna/{qnaId}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 Q&A를 찾을 수 없습니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("QnA 카테고리별 조회 성공")
        fun success_category() {
            mockMvc.perform(
                get("/api/v1/qna/category/{categoryType}", QnaCategoryType.ACCOUNT)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("ACCOUNT 카테고리 Qna 조회 성공"))
                .andExpect(jsonPath("$.data.qna[?(@.categoryType == 'ACCOUNT')].title").isNotEmpty)
                .andDo(print())
        }

        @Test
        @DisplayName("QnA 카테고리별 조회 실패 - 존재하지 않는 카테고리")
        fun fail_invalidCategory() {
            mockMvc.perform(
                get("/api/v1/qna/category/INVALID")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isInternalServerError)
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("QnA 등록 API")
    inner class AddQnaTest {

        @Test
        @DisplayName("QnA 등록 성공")
        fun success() {
            val request = QnaAddRequest(
                "로그인이 안돼요",
                "이메일 인증이 실패합니다.",
                QnaCategoryType.ACCOUNT
            )

            mockMvc.perform(
                post("/api/v1/qna")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("Qna가 생성되었습니다."))
                .andExpect(jsonPath("$.data.title").value("로그인이 안돼요"))
                .andExpect(jsonPath("$.data.categoryType").value("ACCOUNT"))
                .andDo(print())
        }

        @Test
        @DisplayName("QnA 등록 실패 - 제목 누락")
        fun fail_missingTitle() {
            val request = QnaAddRequest(
                "",
                "내용입니다.",
                QnaCategoryType.SYSTEM
            )

            mockMvc.perform(
                post("/api/v1/qna")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Qna 제목은 필수입니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("QnA 등록 실패 - 내용 누락")
        fun fail_missingContent() {
            val request = QnaAddRequest(
                "제목만 있어요",
                "",
                QnaCategoryType.SYSTEM
            )

            mockMvc.perform(
                post("/api/v1/qna")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Qna 내용은 필수입니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("QnA 수정 API")
    inner class UpdateQnaTest {

        @Test
        @DisplayName("QnA 수정 성공")
        fun success() {
            val request = QnaUpdateRequest(
                "수정된 제목",
                "수정된 내용",
                QnaCategoryType.SYSTEM
            )

            mockMvc.perform(
                put("/api/v1/qna/{qnaId}", savedQna.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Qna가 수정되었습니다."))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"))
                .andExpect(jsonPath("$.data.categoryType").value("SYSTEM"))
                .andDo(print())
        }

        @Test
        @DisplayName("QnA 수정 실패 - 답변 등록된 상태")
        fun fail_answered() {
            savedQna.updateQna("제목", "내용", QnaCategoryType.SYSTEM)
            savedQna.registerAnswer("관리자 답변")
            qnaRepository.save(savedQna)

            val request = QnaUpdateRequest(
                "수정 불가 제목",
                "수정 불가 내용",
                QnaCategoryType.SYSTEM
            )

            mockMvc.perform(
                put("/api/v1/qna/{qnaId}", savedQna.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value("관리자가 답변을 완료한 Q&A입니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("QnA 수정 실패 - 존재하지 않는 ID")
        fun fail_notFound() {
            val request = QnaUpdateRequest(
                "수정된 제목",
                "수정된 내용",
                QnaCategoryType.ACCOUNT
            )

            mockMvc.perform(
                put("/api/v1/qna/{qnaId}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 Q&A를 찾을 수 없습니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("QnA 삭제 API")
    inner class DeleteQnaTest {

        @Test
        @DisplayName("QnA 삭제 성공")
        fun success() {
            mockMvc.perform(
                delete("/api/v1/qna/{qnaId}", savedQna.id)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Qna가 삭제되었습니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("QnA 삭제 실패 - 이미 답변 등록된 상태")
        fun fail_answered() {
            savedQna.registerAnswer("관리자 답변")
            qnaRepository.save(savedQna)

            mockMvc.perform(
                delete("/api/v1/qna/{qnaId}", savedQna.id)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value("관리자가 답변을 완료한 Q&A입니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("QnA 삭제 실패 - 존재하지 않는 ID")
        fun fail_notFound() {
            mockMvc.perform(
                delete("/api/v1/qna/{qnaId}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 Q&A를 찾을 수 없습니다."))
                .andDo(print())
        }
    }
}
