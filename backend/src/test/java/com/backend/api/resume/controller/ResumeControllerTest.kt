package com.backend.api.resume.controller;


import com.backend.api.global.JwtTest;
import com.backend.api.resume.dto.request.ResumeCreateRequest;
import com.backend.api.resume.dto.request.ResumeUpdateRequest;
import com.backend.domain.resume.entity.Resume;
import com.backend.domain.resume.repository.ResumeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // security filter disable
@Transactional
class ResumeControllerTest extends JwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResumeRepository resumeRepository;

    @BeforeEach
    void setUp() {
        Resume resume = Resume.builder()
                .content("이력서 내용입니다.")
                .skill("Java, Spring Boot")
                .activity("대외 활동 내용입니다.")
                .certification("없음")
                .career("경력 사항 내용입니다.")
                .portfolioUrl("http://portfolio.example.com")
                .user(mockUser)
                .build();
        resumeRepository.save(resume);
    }

    @Nested
    @DisplayName("이력서 생성 API")
    class t1 {

        @Test
        @DisplayName("정상 작동")
        void success() throws Exception {
            // given
            resumeRepository.deleteAll();
            ResumeCreateRequest request = new ResumeCreateRequest(
                    "이력서 내용입니다.",
                    "Java, Spring Boot",
                    "대외 활동 내용입니다.",
                    "없음",
                    "경력 사항 내용입니다.",
                    "http://portfolio.example.com"
            );

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/users/resumes")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("createResume"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("CREATED"))
                    .andExpect(jsonPath("$.message").value("이력서가 생성되었습니다."))
                    .andExpect(jsonPath("$.data.userId").value(mockUser.getId()))
                    .andExpect(jsonPath("$.data.content").value("이력서 내용입니다."))
                    .andExpect(jsonPath("$.data.skill").value("Java, Spring Boot"))
                    .andExpect(jsonPath("$.data.activity").value("대외 활동 내용입니다."))
                    .andExpect(jsonPath("$.data.certification").value("없음"))
                    .andExpect(jsonPath("$.data.career").value("경력 사항 내용입니다."))
                    .andExpect(jsonPath("$.data.portfolioUrl").value("http://portfolio.example.com"))
                    .andDo(print());
        }

        @Test
        @DisplayName("로그인 안 된 상태에서 요청할 때")
        void fail1() throws Exception {
            // given
            SecurityContextHolder.clearContext();
            ResumeCreateRequest request = new ResumeCreateRequest(
                    "이력서 내용입니다.",
                    "Java, Spring Boot",
                    "대외 활동 내용입니다.",
                    "없음",
                    "경력 사항 내용입니다.",
                    "http://portfolio.example.com"
            );
            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/users/resumes")

                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("createResume"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("로그인된 사용자가 없습니다."))
                    .andDo(print());
        }

        @Test
        @DisplayName("이미 이력서가 존재할 때")
        void fail2() throws Exception {
            // given
            ResumeCreateRequest request = new ResumeCreateRequest(
                    "이력서 내용입니다.",
                    "Java, Spring Boot",
                    "대외 활동 내용입니다.",
                    "없음",
                    "경력 사항 내용입니다.",
                    "http://portfolio.example.com"
            );
            // when
            mockMvc.perform(
                    post("/api/v1/users/resumes")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );

            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/users/resumes")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("createResume"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("이미 등록된 이력서가 있습니다."))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("이력서 수정 API")
    class t2 {
        @Test
        @DisplayName("정상 작동")
        void success() throws Exception {
            //given
            ResumeUpdateRequest request = new ResumeUpdateRequest(
                    "수정된 이력서 내용입니다.",
                    "Java, Spring Boot, mysql",
                    "수정된 대외 활동 내용입니다.",
                    "없음",
                    "수정된 경력 사항 내용입니다.",
                    "http://portfolio.example2.com"
            );

            // when
            ResultActions resultActions = mockMvc.perform(
                    put("/api/v1/users/resumes" )
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("updateResume"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("이력서가 수정되었습니다."))
                    .andExpect(jsonPath("$.data.userId").value(mockUser.getId()))
                    .andExpect(jsonPath("$.data.content").value("수정된 이력서 내용입니다."))
                    .andExpect(jsonPath("$.data.skill").value("Java, Spring Boot, mysql"))
                    .andExpect(jsonPath("$.data.activity").value("수정된 대외 활동 내용입니다."))
                    .andExpect(jsonPath("$.data.certification").value("없음"))
                    .andExpect(jsonPath("$.data.career").value("수정된 경력 사항 내용입니다."))
                    .andExpect(jsonPath("$.data.portfolioUrl").value("http://portfolio.example2.com"))
                    .andDo(print());
        }

        @Test
        @DisplayName("이력서가 존재하지 않을 때")
        void fail1() throws Exception {
            resumeRepository.deleteAll();
            //given
            ResumeUpdateRequest request = new ResumeUpdateRequest(
                    "수정된 이력서 내용입니다.",
                    "Java, Spring Boot, mysql",
                    "수정된 대외 활동 내용입니다.",
                    "없음",
                    "수정된 경력 사항 내용입니다.",
                    "http://portfolio.example2.com"
            );
            // when
            ResultActions resultActions = mockMvc.perform(
                    put("/api/v1/users/resumes")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("updateResume"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("이력서를 찾을 수 없습니다."))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("이력서 삭제 API")
    class t3 {
        @Test
        @DisplayName("정상 작동")
        void success() throws Exception {

            //given
            Resume resume = resumeRepository.findByUser(mockUser)
                    .orElseGet(null);

            System.out.println("resume id = " + resume.getId());

            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/users/resumes/%d" .formatted( resume.getId()))
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("deleteResume"))
                    .andExpect(status().isNoContent())
                    .andExpect(jsonPath("$.status").value("NO_CONTENT"))
                    .andExpect(jsonPath("$.message").value("이력서가 삭제되었습니다."))
                    .andDo(print());
        }

        @Test
        @DisplayName("이력서가 존재하지 않을 때")
        void fail1() throws Exception {
            // given
            Long resumeId = 999L;
            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/users/resumes/%d" .formatted( resumeId))
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("deleteResume"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("이력서를 찾을 수 없습니다."))
                    .andDo(print());
        }

        @Test
        @DisplayName("작성자 불일치")
        void fail2() throws Exception {
            // given
            Long resumeId = 1L;
            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/users/resumes/%d" .formatted(resumeId))
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("deleteResume"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("이력서 수정 권한이 없습니다."))
                    .andDo(print());
        }
        @Test
        @DisplayName("유저가 존재하지 않을 때")
        void fail4() throws Exception {
            // given
            Long resumeId = 1L;
            userRepository.deleteAll();
            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/users/resumes/%d" .formatted( resumeId))
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("deleteResume"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("이력서 조회 API")
    class t4 {
        @Test
        @DisplayName("정상 작동")
        void success() throws Exception {
            //given

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/users/resumes")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("getResume"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("OK"))
                    .andExpect(jsonPath("$.message").value("이력서를 조회했습니다."))
                    .andExpect(jsonPath("$.data.userId").value(mockUser.getId()))
                    .andExpect(jsonPath("$.data.content").value("이력서 내용입니다."))
                    .andExpect(jsonPath("$.data.skill").value("Java, Spring Boot"))
                    .andExpect(jsonPath("$.data.activity").value("대외 활동 내용입니다."))
                    .andExpect(jsonPath("$.data.certification").value("없음"))
                    .andExpect(jsonPath("$.data.career").value("경력 사항 내용입니다."))
                    .andExpect(jsonPath("$.data.portfolioUrl").value("http://portfolio.example.com"))
                    .andDo(print());
        }

        @Test
        @DisplayName("유저가 존재하지 않을 때")
        void fail1() throws Exception {
            // given
            userRepository.deleteAll();
            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/users/resumes")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
            );
            // then
            resultActions
                    .andExpect(handler().handlerType(ResumeController.class))
                    .andExpect(handler().methodName("getResume"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."))
                    .andDo(print());
        }

    }

}