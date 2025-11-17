package com.backend.global.initData

import com.backend.api.answer.dto.request.AnswerCreateRequest
import com.backend.api.answer.service.AnswerService
import com.backend.api.comment.service.CommentService
import com.backend.api.question.service.AdminQuestionService
import com.backend.api.resume.dto.request.ResumeCreateRequest
import com.backend.api.resume.service.ResumeService
import com.backend.api.user.service.UserService
import com.backend.domain.answer.repository.AnswerRepository
import com.backend.domain.post.entity.PinStatus
import com.backend.domain.post.entity.Post
import com.backend.domain.post.entity.PostCategoryType
import com.backend.domain.post.entity.PostStatus
import com.backend.domain.post.repository.PostRepository
import com.backend.domain.qna.entity.Qna
import com.backend.domain.qna.entity.QnaCategoryType
import com.backend.domain.qna.repository.QnaRepository
import com.backend.domain.question.entity.Question
import com.backend.domain.question.entity.QuestionCategoryType
import com.backend.domain.question.repository.QuestionRepository
import com.backend.domain.resume.repository.ResumeRepository
import com.backend.domain.user.entity.Role
import com.backend.domain.user.entity.User
import com.backend.domain.user.entity.VerificationCode
import com.backend.domain.user.repository.UserRepository
import com.backend.domain.user.repository.VerificationCodeRepository
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.stream.IntStream

@Configuration
@RequiredArgsConstructor
class BaseInitData(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val commentService: CommentService,
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
    private val qnaRepository: QnaRepository,
    private val answerService: AnswerService,
    private val passwordEncoder: PasswordEncoder,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val userService: UserService,
    private val resumeService: ResumeService,
    private val adminQuestionService: AdminQuestionService,
    private val resumeRepository: ResumeRepository
) {

    @Autowired
    @Lazy
    private lateinit var self: BaseInitData

    @Bean
    fun initDataRunner(): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments ->
            self.userInitData() // 회원 초기 데이터 등록
            self.initAdminUser()
            self.resumeInitData()
            self.postInitData() // 게시글 초기 데이터 등록
            self.questionInitData() // 질문 초기 데이터 등록
            self.answerInitData() // 답변 초기 데이터 등록
            self.qnaInitData() // QnA 초기 데이터 등록
        }
    }

    @Transactional
    fun userInitData() {
        if (userRepository.count() > 0) {
            return
        }

        // 15개의 이메일 리스트
        val emails = IntStream.rangeClosed(1, 15)
            .mapToObj { i: Int -> "user$i@test.com" }
            .toList()

        for (i in emails.indices) {
            val email = emails[i]

            // 1️⃣ 이메일 인증 코드 더미 데이터 생성
            val verification = VerificationCode(
                email = email,
                code = "INITOK$i", // 임의의 더미 코드
                expiresAt = LocalDateTime.now().plusHours(1),
                verified = true // ✅ 인증 완료 상태로 저장
            )
            verificationCodeRepository.save(verification)

            val user = User(
                email,
                "abc12345",
                "홍길동" + (i + 1),
                "user" + (i + 1),
                20 + (i % 5),
                "https://github.com/user" + (i + 1),
                null,
                Role.USER
            )

            userRepository.save(user)
        }
    }

    @Transactional
    fun initAdminUser() {
        val adminExists = userRepository.existsByRole(Role.ADMIN)

        if (!adminExists) {
            val encodedPassword = passwordEncoder.encode("admin1234")

            val admin = User(
                "admin@naver.com",
                encodedPassword,
                "관리자",
                "admin",
                30,
                "https://github.com/admin",
                null,
                Role.ADMIN
            )

            userRepository.save(admin)
        }
    }

    fun resumeInitData() {
        if (resumeRepository.count() > 0) return

        resumeService.createResume(
            1L, ResumeCreateRequest(
                "안녕하세요. 소프트웨어 엔지니어로서 다양한 프로젝트 경험을 쌓아왔습니다. 유지보수 및 신규 기능 개발 모두 경험했습니다.",
                "Java, Spring Boot, JPA, MySQL, Redis, React, TypeScript",
                "ACM-ICPC 대학생 프로그래밍 경진대회 참가, 오픈소스 프로젝트 기여",
                "정보처리기사, AWS Certified Solutions Architect – Associate",
                "삼성전자 백엔드 인턴 (2023.06 ~ 2023.08), 스타트업 웹 개발 인턴 (2022.12 ~ 2023.02)",
                "http://portfolio.example.com/myportfolio"
            )
        )
    }

    @Transactional
    fun postInitData() {
        if (postRepository.count() > 0) return

        val user1 = userRepository.findById(1L)
            .orElseThrow { RuntimeException("User 1L not found for Post Init") }
        val user2 = userRepository.findById(2L)
            .orElseThrow { RuntimeException("User 2L not found for Post Init") }
        val user3 = userRepository.findById(3L)
            .orElseThrow { RuntimeException("User 3L not found for Post Init") }
        val user4 = userRepository.findById(4L)
            .orElseThrow { RuntimeException("User 4L not found for Post Init") }


        // ---------- 프로젝트 ----------
        val post1 = Post(
            title = "Next.js 14 프로젝트 팀원 모집",
            introduction = "Next.js 14 App Router를 활용한 커머스 사이트 제작 프로젝트입니다. 디자이너 1명, 프론트엔드 2명 모집 중입니다.",
            content = """
                ## 프로젝트 소개
                Next.js 14의 App Router와 최신 기능을 활용하여 반응형 커머스 사이트를 제작하려 합니다.
                실시간 장바구니, 결제, 관리자 페이지까지 구현할 예정입니다.
        
                ## 기술 스택
                - Front: Next.js 14, TypeScript, Tailwind CSS
                - Back: Spring Boot / Supabase
                - 협업: GitHub, Notion, Figma
        
                ## 일정
                3개월 예상 (주 2회 미팅, 온라인 중심)
        
                ## 모집 인원
                - 디자이너 1명
                - 프론트엔드 2명
                - 백엔드 1명
        
                ## 지원 방법
                댓글로 간단한 자기소개와 포트폴리오 링크 남겨주세요.
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(7),
            status = PostStatus.ING,
            pinStatus = PinStatus.PINNED,
            recruitCount = 5,
            users = user1,
            postCategoryType = PostCategoryType.PROJECT
        )
        postRepository.save(post1)

        val post2 = Post(
            title = "Spring Boot + React 협업 프로젝트 팀 모집",
            introduction = "Spring Boot 백엔드와 React 프론트엔드로 포트폴리오용 SNS 서비스를 개발할 팀을 모집합니다.",
            content = """
                ## 프로젝트 개요
                사용자 인증, 피드, 댓글, 좋아요 등 SNS 주요 기능을 직접 구현합니다.
                실제 배포까지 목표로 합니다.
        
                ## 기술 스택
                - Backend: Spring Boot 3, JPA, QueryDSL, JWT
                - Frontend: React 18, Vite, Tailwind CSS
                - Infra: AWS EC2, S3, GitHub Actions
        
                ## 진행 방식
                - Notion으로 일정 관리
                - Discord 소통
                - GitHub Flow로 협업
        
                ## 모집 인원
                - 백엔드 2명
                - 프론트엔드 2명
        
                ## 일정
                약 2개월 예상
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(10),
            status = PostStatus.ING,
            pinStatus = PinStatus.PINNED,
            recruitCount = 4,
            users = user1,
            postCategoryType = PostCategoryType.PROJECT
        )
        postRepository.save(post2)

        val post3 = Post(
            title = "AI 챗봇 개발 프로젝트 함께하실 분 구합니다",
            introduction = "OpenAI API와 Spring Boot를 활용해 간단한 AI 챗봇 웹서비스를 제작하는 프로젝트입니다.",
            content = """
                ## 프로젝트 소개
                AI 기술을 서비스로 녹여내는 경험을 쌓기 위한 소규모 팀 프로젝트입니다.
                GPT API를 사용해 Q&A 챗봇을 구현하고, RAG 기반 지식 검색 기능도 추가할 예정입니다.
        
                ## 기술 스택
                - Backend: Spring Boot, Spring AI, JPA
                - Frontend: Next.js, TypeScript
                - AI: OpenAI API (GPT-4)
                - Infra: Vercel, AWS
        
                ## 일정
                - 약 6주간 진행
                - 주 1회 회의
        
                ## 모집 인원
                - 백엔드 1명
                - 프론트엔드 1명
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(8),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 4,
            users = user1,
            postCategoryType = PostCategoryType.PROJECT
        )
        postRepository.save(post3)

        val post4 = Post(
            title = "Flutter 기반 가계부 앱 사이드 프로젝트",
            introduction = "Flutter로 간단한 가계부 앱을 만들고 싶은 분들을 모집합니다. 초보자도 환영합니다.",
            content = """
                ## 프로젝트 목표
                Flutter로 크로스플랫폼 앱을 직접 배포해보는 경험을 쌓습니다.
                Firebase 인증 및 Cloud Firestore 연동 예정입니다.
        
                ## 기술 스택
                - Flutter 3.x
                - Firebase Auth / Firestore
                - GitHub, Figma 협업
        
                ## 모집 인원
                - Flutter 개발자 2명
                - 디자이너 1명
        
                ## 일정
                6주 진행 / 주 2회 미팅
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(11),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 4,
            users = user1,
            postCategoryType = PostCategoryType.PROJECT
        )
        postRepository.save(post4)

        val post5 = Post(
            title = "Node.js 기반 REST API 서버 개발팀 모집",
            introduction = "Express.js를 기반으로 한 REST API 서버를 함께 개발할 팀원을 찾습니다.",
            content = """
                ## 프로젝트 개요
                인증, CRUD, 파일 업로드 등 REST API 기본기를 실습하며 함께 성장합니다.
                Swagger 문서화를 포함한 실무형 프로젝트로 진행합니다.
        
                ## 기술 스택
                - Node.js (Express)
                - MongoDB, Mongoose
                - Swagger, JWT
        
                ## 일정
                5주간 주 1회 회의
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(9),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 5,
            users = user1,
            postCategoryType = PostCategoryType.PROJECT
        )
        postRepository.save(post5)

        val post6 = Post(
            title = "Unity 3D 인디 게임 개발팀 모집",
            introduction = "Unity로 간단한 로그라이크 게임을 제작할 개발자, 디자이너를 모집합니다.",
            content = """
                ## 프로젝트 소개
                2D 기반 로그라이크 게임을 3개월 내 출시 목표로 진행합니다.
                간단한 전투 시스템과 맵 생성 알고리즘을 구현합니다.
        
                ## 기술 스택
                - Unity 2022
                - C#
                - GitHub, Trello
        
                ## 모집 인원
                - 프로그래머 2명
                - 그래픽 디자이너 1명
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(15),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 4,
            users = user1,
            postCategoryType = PostCategoryType.PROJECT
        )
        postRepository.save(post6)

        val post7 = Post(
            title = "Spring 기반 쇼핑몰 REST API 개발 프로젝트",
            introduction = "Spring Boot와 JPA로 쇼핑몰 API 서버를 개발합니다. 실무 감각을 익히고 싶은 분 환영합니다.",
            content = """
                ## 프로젝트 개요
                상품, 장바구니, 주문, 회원 시스템을 직접 설계하고 구현합니다.
                Swagger 문서, 테스트 코드 작성까지 포함합니다.
        
                ## 기술 스택
                - Java 21 / Spring Boot 3
                - JPA, QueryDSL
                - MySQL, Redis
                - Swagger / JUnit 5
        
                ## 일정
                2개월 예정, 온라인 진행
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(10),
            status = PostStatus.ING,
            pinStatus = PinStatus.PINNED,
            recruitCount = 5,
            users = user1,
            postCategoryType = PostCategoryType.PROJECT
        )
        postRepository.save(post7)
        commentService.writeComment(
            user1, post7.id,
            "백엔드 지원합니다. Spring Boot 기반 쇼핑몰 API 개발 경험 있으며, " +
                    "JPA, MySQL, Redis 활용과 테스트 코드 작성까지 가능합니다."
        )

        val post8 = Post(
            title = "AI 이미지 생성 웹서비스 프로젝트",
            introduction = "Stable Diffusion API를 활용한 이미지 생성 웹 프로젝트입니다.",
            content = """
                ## 프로젝트 목표
                텍스트 입력을 통해 이미지를 생성하는 웹 애플리케이션을 개발합니다.
                프롬프트 관리, 이미지 저장, 히스토리 기능 구현 예정입니다.
        
                ## 기술 스택
                - Front: Next.js, Tailwind CSS
                - Back: Flask or FastAPI
                - AI: Stable Diffusion API
        
                ## 일정
                8주 예상 / 주 1회 회의
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(14),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 5,
            users = user1,
            postCategoryType = PostCategoryType.PROJECT
        )
        postRepository.save(post8)
        commentService.writeComment(
            user1, post8.id,
            "백엔드 지원합니다. Flask/FastAPI 기반 API 개발 경험이 있으며, 이미지 생성 요청 처리 및 " +
                    "히스토리 저장 기능 구현이 가능합니다. 안정적인 서비스 운영에도 자신 있습니다."
        )

        val post9 = Post(
            title = "CS 기초 스터디 (운영체제, 네트워크, DB)",
            introduction = "컴퓨터공학 기초 지식을 체계적으로 학습할 스터디입니다.",
            content = """
                ## 스터디 개요
                운영체제, 네트워크, 데이터베이스의 핵심 개념을 교재 기반으로 정리하고 발표합니다.
        
                ## 진행 방식
                - 매주 토요일 오후 2시 온라인
                - 발표자 순번제
                - 스터디 후 질의응답 및 피드백
        
                ## 교재
                - 『Operating System Concepts』
                - 『그림으로 배우는 네트워크』
                - 『Database System Concepts』
        
                ## 모집 인원
                총 6명
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(12),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 6,
            users = user1,
            postCategoryType = PostCategoryType.STUDY
        )
        postRepository.save(post9)

        val post10 = Post(
            title = "Spring Boot 스터디 – 실무 프로젝트 따라 만들기",
            introduction = "Spring Boot로 블로그 서비스 만들어보며 JPA와 MVC를 익히는 스터디입니다.",
            content = """
                ## 스터디 내용
                블로그 CRUD, 로그인, 댓글 기능을 직접 구현하면서 스프링의 핵심을 배웁니다.
        
                ## 진행
                - 매주 목요일 20시, 온라인
                - 코드 리뷰 및 과제 제출
        
                ## 기술 스택
                - Java 21
                - Spring Boot 3, JPA
                - Thymeleaf or React (선택)
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(9),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 5,
            users = user1,
            postCategoryType = PostCategoryType.STUDY
        )
        postRepository.save(post10)

        val post11 = Post(
            title = "알고리즘 스터디 – 백준 플래 목표반",
            introduction = "매주 5문제씩 풀고 리뷰하는 집중형 알고리즘 스터디입니다.",
            content = """
                ## 스터디 목표
                백준 플래티넘 레벨 달성을 목표로 합니다.
        
                ## 진행 방식
                - 문제 난이도: 골드~플래
                - 매주 일요일 온라인 리뷰
                - PR 리뷰 후 피드백
        
                ## 사용 언어
                Java, Python 자유
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(7),
            status = PostStatus.ING,
            pinStatus = PinStatus.PINNED,
            recruitCount = 6,
            users = user1,
            postCategoryType = PostCategoryType.STUDY
        )
        postRepository.save(post11)

        val post12 = Post(
            title = "React + TypeScript 프론트엔드 스터디",
            introduction = "React로 포트폴리오를 완성하는 것을 목표로 하는 스터디입니다.",
            content = """
                ## 스터디 목표
                React의 주요 개념을 익히고, 실제 프로젝트 클론코딩까지 진행합니다.
        
                ## 커리큘럼
                1. Hooks, Context
                2. Router, Zustand
                3. API 연동
                4. 프로젝트 실습
        
                ## 진행
                - 주 1회 화상 모임
                - Notion으로 과제 관리
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(13),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 6,
            users = user1,
            postCategoryType = PostCategoryType.STUDY
        )
        postRepository.save(post12)

        val post13 = Post(
            title = "코딩테스트 대비 자바 스터디",
            introduction = "자바로 코딩테스트를 준비하는 개발자 대상 스터디입니다.",
            content = """
                ## 스터디 방식
                - 백준, 프로그래머스 문제 풀이
                - 주 3문제 / 주말 리뷰 세션
                - Discord 실시간 피드백
        
                ## 교재
                『Do it! 알고리즘 코딩테스트 자바편』
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(10),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 5,
            users = user1,
            postCategoryType = PostCategoryType.STUDY
        )
        postRepository.save(post13)

        val post14 = Post(
            title = "면접 대비 CS 질문 스터디",
            introduction = "대기업·스타트업 면접을 대비한 기술 질문 중심 스터디입니다.",
            content = """
                ## 스터디 주제
                - OS, DB, Network, 자료구조, Java
                - 매주 1인당 3문제 준비 및 발표
        
                ## 진행
                - 매주 화/금 저녁
                - Google Meet으로 진행
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(11),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 6,
            users = user2,
            postCategoryType = PostCategoryType.STUDY
        )
        postRepository.save(post14)
        commentService.writeComment(
            user2, post14.id,
            "스터디 참여하고 싶습니다. OS, DB, Network, 자료구조, Java 관련 질문 준비 경험이 있으며, " +
                    "매주 문제를 준비하고 발표하며 함께 공부하는 것에 적극적입니다."
        )

        val post15 = Post(
            title = "Git & 협업 워크플로우 스터디",
            introduction = "Git/GitHub 협업 프로세스를 익히는 실습형 스터디입니다.",
            content = """
                ## 내용
                - Git 기본 명령어 및 브랜치 전략
                - GitHub Flow 실습
                - PR 리뷰 문화 체득
        
                ## 진행
                - 4주 과정
                - 실습 중심, 과제 제출 필수
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(8),
            status = PostStatus.ING,
            pinStatus = PinStatus.NOT_PINNED,
            recruitCount = 6,
            users = user2,
            postCategoryType = PostCategoryType.STUDY
        )
        postRepository.save(post15)
        commentService.writeComment(
            user2, post15.id,
            "스터디 참여 지원합니다. Git과 GitHub Flow 사용 경험이 있으며, 브랜치 전략과 PR 리뷰 과정에 적극적으로 참여할 준비가 되어 있습니다."
        )

        val post16 = Post(
            title = "Next.js 14 프로젝트 팀원 모집",
            introduction = "Next.js 14 App Router를 활용한 커머스 사이트 제작 프로젝트입니다. 디자이너 1명, 프론트엔드 개발자 2명을 찾습니다.",
            content = """
                ## 프로젝트 소개
                Next.js 14의 최신 기능을 활용하여 현대적인 커머스 사이트를 제작하는 프로젝트입니다.
        
                ## 모집 인원
                - 디자이너 1명
                - 프론트엔드 개발자 2명
        
                ## 필요 기술
                - Next.js 14 (App Router)
                - TypeScript
                - Tailwind CSS
                - Supabase (선택)
        
                ## 진행 방식
                - 주 2회 온라인 미팅
                - GitHub로 협업, Notion으로 문서 관리
            """.trimIndent(),
            deadline = LocalDateTime.now().plusDays(7),
            status = PostStatus.ING,
            pinStatus = PinStatus.PINNED,
            recruitCount = 5,
            users = user2,
            postCategoryType = PostCategoryType.PROJECT
        )
        postRepository.save(post16)

        commentService.writeComment(user3, post16.id, "프로젝트 흥미롭네요!")
        commentService.writeComment(user4, post16.id, "혹시 백엔드 포지션도 모집하시나요?")
    }

    @Transactional
    fun questionInitData() {
        if (questionRepository.count() > 0) {
            return
        }
        val user1 =
            userRepository.findById(1L).orElseThrow { RuntimeException("User 1L not found for Question Init") }

        val q1 = Question(
            title = "운영체제에서 프로세스와 스레드의 차이점은 무엇인가요?",
            content = "프로세스와 스레드는 모두 실행 단위를 나타내지만, 메모리 구조나 자원 공유 방식이 다릅니다. 각각의 차이점을 설명해주세요.",
            author = user1,
            score = 25,
            categoryType = QuestionCategoryType.OS
        )
        questionRepository.save(q1)
        adminQuestionService.approveQuestion(q1.id, true, userRepository.findById(16L).get())

        val q2 = Question(
            title = "데이터베이스에서 인덱스(Index)는 어떤 역할을 하나요?",
            content = "인덱스를 사용하면 검색 속도가 빨라지지만, 삽입/삭제 시 오버헤드가 생깁니다. 인덱스의 동작 원리와 장단점을 설명해주세요.",
            author = user1,
            score = 45,
            categoryType = QuestionCategoryType.DATABASE
        )
        questionRepository.save(q2)
        adminQuestionService.approveQuestion(q2.id, true, userRepository.findById(16L).get())

        val q3 = Question(
            title = "TCP와 UDP의 차이점을 설명해주세요.",
            content = "두 프로토콜의 연결 방식, 신뢰성, 속도 측면에서의 차이와 각각이 주로 사용되는 사례를 알려주세요.",
            author = user1,
            score = 35,
            categoryType = QuestionCategoryType.NETWORK
        )
        questionRepository.save(q3)
        adminQuestionService.approveQuestion(q3.id, true, userRepository.findById(16L).get())

        val q4 = Question(
            title = "자바의 Garbage Collection은 어떻게 동작하나요?",
            content = "GC의 기본 원리와 주요 알고리즘(Mark and Sweep, Generational GC 등)을 설명하고, 성능 최적화 방법에 대해 설명해주세요.",
            author = user1,
            score = 45,
            categoryType = QuestionCategoryType.ALGORITHM
        )
        questionRepository.save(q4)
        adminQuestionService.approveQuestion(q4.id, true, userRepository.findById(16L).get())

        val q5 = Question(
            title = "데드락(Deadlock)은 어떤 상황에서 발생하나요?",
            content = "데드락의 4가지 필요 조건과, 이를 예방하거나 해결할 수 있는 방법을 구체적으로 설명해주세요.",
            author = user1,
            score = 10,
            categoryType = QuestionCategoryType.OS
        )
        questionRepository.save(q5)
        adminQuestionService.approveQuestion(q5.id, true, userRepository.findById(16L).get())

        val q6 = Question(
            title = "자료구조에서 연결 리스트와 배열의 차이점은 무엇인가요?",
            content = "연결 리스트와 배열의 구조적 차이와 장단점, 삽입/삭제/검색 시 성능 차이를 설명해주세요.",
            author = user1,
            score = 35,
            categoryType = QuestionCategoryType.DATA_STRUCTURE
        )
        questionRepository.save(q6)
        adminQuestionService.approveQuestion(q6.id, true, userRepository.findById(16L).get())

        val q7 = Question(
            title = "데이터베이스에서 정규화(Normalization)와 비정규화(Denormalization)의 차이점은 무엇인가요?",
            content = "정규화와 비정규화의 개념과 목적을 설명하고, 각각을 적용할 때 장단점과 실제 활용 사례를 예시와 함께 설명해주세요.",
            author = user1,
            score = 45,
            categoryType = QuestionCategoryType.DATABASE
        )
        questionRepository.save(q7)
        adminQuestionService.approveQuestion(q7.id, true, userRepository.findById(16L).get())

        val q8 = Question(
            title = "Java 컬렉션에서 equals()와 hashCode()가 중요한 이유는 무엇인가요?",
            content = "HashMap, HashSet 등 해시 기반 컬렉션에서 equals()와 hashCode()의 역할과 상호관계를 설명해주세요. 또한 두 메서드가 일관되지 않을 때 발생할 수 있는 문제와 예시를 들어주세요.",
            author = user1,
            score = 50,
            categoryType = QuestionCategoryType.DATA_STRUCTURE
        )
        questionRepository.save(q8)
        adminQuestionService.approveQuestion(q8.id, true, userRepository.findById(16L).get())

        val q9 = Question(
            title = "HTTP 상태 코드 301, 302, 404, 500의 차이를 설명해주세요.",
            content = "각 상태 코드의 의미와 주로 사용되는 상황을 설명해주세요. 특히 301과 302의 리다이렉션 차이, 404와 500의 서버/클라이언트 오류 차이도 함께 설명해주세요.",
            author = user1,
            score = 25,
            categoryType = QuestionCategoryType.NETWORK
        )
        questionRepository.save(q9)
        adminQuestionService.approveQuestion(q9.id, true, userRepository.findById(16L).get())

        val q10 = Question(
            title = "멀티스레드 환경에서의 동기화(Synchronization)는 왜 필요한가요?",
            content = "동기화의 필요성과 synchronized 키워드, ReentrantLock 등의 사용 방법을 설명해주세요. 또한 과도한 동기화로 인해 발생할 수 있는 성능 문제에 대해서도 논의해주세요.",
            author = user1,
            score = 50,
            categoryType = QuestionCategoryType.OS
        )
        questionRepository.save(q10)

        adminQuestionService.approveQuestion(q10.id, true, userRepository.findById(16L).get())
    }

    @Transactional
    fun answerInitData() {
        if (answerRepository.count() > 0) {
            return
        }
        val user1 = userRepository.findById(1L).orElseThrow { RuntimeException("User 1L not found for Answer Init") }
        val user2 = userRepository.findById(2L).orElseThrow { RuntimeException("User 2L not found for Answer Init") }
        val user3 = userRepository.findById(3L).orElseThrow { RuntimeException("User 3L not found for Answer Init") }
        val user4 = userRepository.findById(4L).orElseThrow { RuntimeException("User 4L not found for Answer Init") }
        val user5 = userRepository.findById(5L).orElseThrow { RuntimeException("User 5L not found for Answer Init") }
        val user6 = userRepository.findById(6L).orElseThrow { RuntimeException("User 6L not found for Answer Init") }
        val user7 = userRepository.findById(7L).orElseThrow { RuntimeException("User 7L not found for Answer Init") }
        val user8 = userRepository.findById(8L).orElseThrow { RuntimeException("User 8L not found for Answer Init") }
        val user9 = userRepository.findById(9L).orElseThrow { RuntimeException("User 9L not found for Answer Init") }
        val user10 = userRepository.findById(10L).orElseThrow {
            RuntimeException(
                "User 10L not found for Answer Init"
            )
        }
        val user11 = userRepository.findById(11L).orElseThrow {
            RuntimeException(
                "User 11L not found for Answer Init"
            )
        }
        val user12 = userRepository.findById(12L).orElseThrow {
            RuntimeException(
                "User 12L not found for Answer Init"
            )
        }
        val user13 = userRepository.findById(13L).orElseThrow {
            RuntimeException(
                "User 13L not found for Answer Init"
            )
        }
        val user14 = userRepository.findById(14L).orElseThrow {
            RuntimeException(
                "User 14L not found for Answer Init"
            )
        }
        val user15 = userRepository.findById(15L).orElseThrow {
            RuntimeException(
                "User 15L not found for Answer Init"
            )
        }

        answerService.writeAnswer(
            user1, 1L, AnswerCreateRequest(
                """
        프로세스는 독립된 메모리 공간과 자원을 가진 실행 단위로, 시스템적으로 보호됩니다. 반면 스레드는 동일 프로세스 내에서 메모리와 자원을 공유하며 경량화된 실행 단위입니다. 스레드는 생성과 문맥 전환이 빠르지만, 공유 자원 접근 시 동기화가 필요합니다. 따라서 멀티스레드 환경에서는 스레드 안전성을 고려한 설계가 필수적입니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user1, 2L, AnswerCreateRequest(
                """
        인덱스는 데이터베이스에서 특정 컬럼 값을 효율적으로 검색할 수 있도록 지원하는 자료 구조입니다. 이를 통해 SELECT 쿼리 성능이 크게 향상되지만, INSERT, UPDATE, DELETE 시 인덱스 갱신 비용이 발생합니다. 적절한 컬럼 선택과 인덱스 구조(B-Tree, Hash 등) 이해가 필수적이며, 균형 잡힌 설계가 성능 최적화의 핵심입니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user1, 3L, AnswerCreateRequest(
                """
        TCP는 연결 지향 프로토콜로 데이터 순서 보장과 오류 검출, 재전송 기능을 제공하여 신뢰성을 보장하지만, 속도가 느릴 수 있습니다. UDP는 비연결형으로 송수신 속도가 빠르지만 데이터 손실과 순서 보장이 없습니다. TCP는 웹, 이메일, 파일 전송 등에 적합하며, UDP는 스트리밍, 온라인 게임, VoIP 등 실시간성이 중요한 서비스에 주로 사용됩니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user2, 3L, AnswerCreateRequest(
                """
        TCP는 연결 지향 프로토콜로 데이터 순서 보장과 오류 검출, 재전송 기능을 제공하여 신뢰성을 보장하지만, 속도가 느릴 수 있습니다. UDP는 비연결형으로 송수신 속도가 빠르지만 데이터 손실과 순서 보장이 없습니다. TCP는 웹, 이메일, 파일 전송 등에 적합하며, UDP는 스트리밍, 온라인 게임, VoIP 등 실시간성이 중요한 서비스에 주로 사용됩니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user3, 10L, AnswerCreateRequest(
                """
        멀티스레드 환경에서는 여러 스레드가 동시에 자원에 접근하면 데이터 불일치가 발생할 수 있습니다. synchronized나 Lock을 이용해 임계 구역을 설정하면 한 번에 하나의 스레드만 접근하게 되어 데이터의 일관성을 유지할 수 있습니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user4, 10L, AnswerCreateRequest(
                """
        동기화는 공유 자원에 대한 동시 접근을 제어하기 위한 핵심 개념입니다. 이를 통해 Race Condition을 방지하고 프로그램의 예측 가능한 동작을 보장합니다. 하지만 너무 많은 동기화는 성능 저하를 유발할 수 있습니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user5, 10L, AnswerCreateRequest(
                """
        멀티스레드에서 동기화는 데이터 무결성을 보장하기 위한 수단입니다. 여러 스레드가 같은 변수를 동시에 변경할 때 충돌이 일어나므로 synchronized 블록으로 임계 영역을 보호해야 합니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user6, 10L, AnswerCreateRequest(
                """
        여러 스레드가 동시에 실행될 때 동일한 자원에 접근하면 예상치 못한 값이 나올 수 있습니다. 이런 상황을 막기 위해 synchronized 키워드나 ReentrantLock을 사용해 순서를 제어합니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user7, 10L, AnswerCreateRequest(
                """
        스레드가 동시에 변수에 접근하면 데이터가 꼬일 수 있어서 동기화가 필요합니다. 동기화를 하면 한 스레드가 작업하는 동안 다른 스레드가 기다리게 됩니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user8, 10L, AnswerCreateRequest(
                """
        동기화는 멀티스레드 환경에서 데이터가 꼬이지 않게 하는 기능이에요. 예를 들어 여러 스레드가 카운트를 동시에 증가시키면 값이 맞지 않기 때문에 순서대로 실행되도록 해야 합니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user9, 10L, AnswerCreateRequest(
                """
        공유 자원을 동시에 쓰면 값이 달라질 수 있으니까 동기화를 합니다. 하지만 너무 많이 걸면 속도가 느려져요. 그래서 꼭 필요한 부분만 쓰는 게 좋아요.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user10, 10L, AnswerCreateRequest(
                """
        스레드 여러 개가 하나의 변수를 수정할 때 값이 이상해질 수 있어서 동기화가 필요합니다. synchronized를 쓰면 한 번에 한 스레드만 접근합니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user11, 10L, AnswerCreateRequest(
                """
        동기화는 여러 스레드가 동시에 같은 데이터를 변경하지 못하게 하는 거예요. 그걸 안 하면 값이 엉망이 돼요.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user12, 10L, AnswerCreateRequest(
                """
        스레드가 많으면 순서가 꼬일 수 있어서 synchronized로 막는 거예요. 근데 너무 막으면 느려지니까 적당히 해야 합니다.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user13, 10L, AnswerCreateRequest(
                """
        스레드가 많으면 서로 간섭해서 값이 잘못될 수 있어요. 그래서 잠깐씩 순서를 정해서 실행하게 하는 게 동기화예요.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user14, 10L, AnswerCreateRequest(
                """
        여러 스레드가 한 데이터에 동시에 접근하면 문제가 생길 수 있으니까 막는 거죠.
        
        """.trimIndent(), true
            )
        )
        answerService.writeAnswer(
            user15, 10L, AnswerCreateRequest(
                """
        스레드끼리 동시에 실행돼서 값이 꼬이는 걸 막기 위해 동기화를 해요.
        
        """.trimIndent(), true
            )
        )
    }

    @Transactional
    fun qnaInitData() {
        if (qnaRepository.count() > 0) {
            return
        }

        val user1 = userRepository.findById(1L).orElseThrow { RuntimeException("User 1L not found for Qna Init") }
        val user2 = userRepository.findById(2L).orElseThrow { RuntimeException("User 2L not found for Qna Init") }

        val qna1 = Qna(
            title = "로그인이 자꾸 실패합니다.",
            content = "회원가입은 정상적으로 되었는데, 로그인 시 '이메일 또는 비밀번호가 올바르지 않습니다'라는 문구가 계속 나옵니다.",
            author = user1,
            categoryType = QnaCategoryType.ACCOUNT
        )

        val qna2 = Qna(
            title = "프리미엄 멤버십 결제 관련 문의드립니다.",
            content = "결제 완료 후에도 프리미엄 기능이 활성화되지 않습니다.",
            author = user2,
            categoryType = QnaCategoryType.PAYMENT
        )

        val qna3 = Qna(
            title = "사이트 접속 시 오류가 발생합니다.",
            content = "크롬 브라우저에서 접속 시 자꾸 500 오류가 뜹니다.",
            author = user1,
            categoryType = QnaCategoryType.SYSTEM
        )

        val qna4 = Qna(
            title = "팀 모집글 관련 문의드립니다.",
            content = "모집글 작성 시 마감일을 수정할 수 있나요?",
            author = user2,
            categoryType = QnaCategoryType.RECRUITMENT
        )

        val qna5 = Qna(
            title = "사이트 개선 제안드립니다.",
            content = "Q&A 게시판에 검색 기능이 추가되면 좋겠습니다.",
            author = user1,
            categoryType = QnaCategoryType.SUGGESTION
        )


        qnaRepository.save(qna1)
        qnaRepository.save(qna2)
        qnaRepository.save(qna3)
        qnaRepository.save(qna4)
        qnaRepository.save(qna5)
    }
}