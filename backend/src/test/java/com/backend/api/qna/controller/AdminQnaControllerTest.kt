package com.backend.api.qna.controller

import com.backend.api.global.JwtTest
import com.backend.api.qna.dto.request.QnaAnswerRequest
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
class AdminQnaControllerTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val qnaRepository: QnaRepository,
    private val userRepository: UserRepository,
) : JwtTest() {

    @MockBean
    private lateinit var rq: Rq

    private lateinit var adminUser: User
    private lateinit var savedQna: Qna

    @BeforeEach
    fun setUp() {
        adminUser = userRepository.save(
            User.builder()
                .name("관리자")
                .email("admin@test.com")
                .nickname("admin")
                .github("https://github.com/admin")
                .password("1234")
                .role(Role.ADMIN)
                .build()
        )

        Mockito.`when`(rq.getUser()).thenReturn(adminUser)

        savedQna = qnaRepository.save(
            Qna.builder()
                .title("Qna 제목")
                .content("Qna 내용")
                .author(adminUser)
                .categoryType(QnaCategoryType.SYSTEM)
                .build()
        )
    }

    @Nested
    @DisplayName("Qna 전체 조회 API")
    inner class GetAllQnaApiTest {

        @Test
        @DisplayName("전체 Qna 조회 성공")
        fun success() {
            mockMvc.perform(
                get("/api/v1/admin/qna")
                    .param("page", "1")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Qna 전체 조회 성공"))
                .andExpect(jsonPath("$.data.qna[0].title").value("Qna 제목"))
                .andExpect(jsonPath("$.data.qna[0].categoryType").value("SYSTEM"))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("Qna 단건 조회 API")
    inner class GetQnaApiTest {

        @Test
        @DisplayName("단건 Qna 조회 성공")
        fun success() {
            mockMvc.perform(
                get("/api/v1/admin/qna/{qnaId}", savedQna.id)
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("${savedQna.id}번 Qna 조회 성공"))
                .andExpect(jsonPath("$.data.title").value("Qna 제목"))
                .andExpect(jsonPath("$.data.content").value("Qna 내용"))
                .andDo(print())
        }

        @Test
        @DisplayName("단건 Qna 조회 실패 - 존재하지 않는 ID")
        fun fail_notFound() {
            mockMvc.perform(
                get("/api/v1/admin/qna/{qnaId}", 999L)
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 Q&A를 찾을 수 없습니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("Qna 카테고리별 조회 API")
    inner class GetQnaByCategoryApiTest {

        @Test
        @DisplayName("카테고리별 Qna 조회 성공")
        fun success_category() {
            mockMvc.perform(
                get("/api/v1/admin/qna/category/{categoryType}", QnaCategoryType.SYSTEM)
                    .param("page", "1")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("SYSTEM 카테고리 Qna 조회 성공"))
                .andExpect(jsonPath("$.data.qna[?(@.categoryType == 'SYSTEM')].title").isNotEmpty)
                .andDo(print())
        }

        @Test
        @DisplayName("카테고리별 Qna 조회 실패 - 잘못된 카테고리 파라미터")
        fun fail_invalidCategory() {
            mockMvc.perform(
                get("/api/v1/admin/qna/category/INVALID")
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isInternalServerError)
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("Qna 답변 등록 API")
    inner class RegisterAnswerApiTest {

        @Test
        @DisplayName("답변 등록 성공")
        fun success() {
            val request = QnaAnswerRequest("관리자 답변입니다.")

            mockMvc.perform(
                put("/api/v1/admin/qna/{qnaId}/answer", savedQna.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Qna 답변이 등록되었습니다."))
                .andExpect(jsonPath("$.data.adminAnswer").value("관리자 답변입니다."))
                .andExpect(jsonPath("$.data.isAnswered").value(true))
                .andDo(print())
        }

        @Test
        @DisplayName("답변 등록 실패 - 이미 답변이 존재함")
        fun fail_alreadyAnswered() {
            savedQna.registerAnswer("기존 답변")
            qnaRepository.save(savedQna)

            val request = QnaAnswerRequest("새 답변입니다.")

            mockMvc.perform(
                put("/api/v1/admin/qna/{qnaId}/answer", savedQna.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("이미 답변이 등록된 Q&A입니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("답변 등록 실패 - 존재하지 않는 Qna ID")
        fun fail_notFound() {
            val request = QnaAnswerRequest("관리자 답변입니다.")

            mockMvc.perform(
                put("/api/v1/admin/qna/{qnaId}/answer", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 Q&A를 찾을 수 없습니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("답변 등록 실패 - 요청 본문 유효성 실패(answer 공백)")
        fun fail_blankAnswer() {
            val request = QnaAnswerRequest("")

            mockMvc.perform(
                put("/api/v1/admin/qna/{qnaId}/answer", savedQna.id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Qna 답변은 필수입니다."))
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("Qna 삭제 API")
    inner class DeleteQnaApiTest {

        @Test
        @DisplayName("Qna 삭제 성공")
        fun success() {
            mockMvc.perform(
                delete("/api/v1/admin/qna/{qnaId}", savedQna.id)
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Qna가 삭제되었습니다."))
                .andDo(print())
        }

        @Test
        @DisplayName("Qna 삭제 실패 - 존재하지 않는 ID")
        fun fail_notFound() {
            mockMvc.perform(
                delete("/api/v1/admin/qna/{qnaId}", 999L)
                    .accept(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 Q&A를 찾을 수 없습니다."))
                .andDo(print())
        }
    }
}
