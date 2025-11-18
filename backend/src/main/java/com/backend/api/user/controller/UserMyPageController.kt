package com.backend.api.user.controller

import com.backend.api.answer.dto.response.AnswerMypageResponse
import com.backend.api.answer.dto.response.AnswerPageResponse
import com.backend.api.answer.service.AnswerService
import com.backend.api.comment.dto.response.CommentMypageResponse
import com.backend.api.comment.dto.response.CommentPageResponse
import com.backend.api.comment.service.CommentService
import com.backend.api.post.dto.response.PostPageResponse
import com.backend.api.post.dto.response.PostResponse
import com.backend.api.post.service.PostService
import com.backend.api.question.dto.response.QuestionPageResponse
import com.backend.api.question.dto.response.QuestionResponse
import com.backend.api.question.service.QuestionService
import com.backend.api.user.dto.request.MyPageRequest
import com.backend.api.user.dto.response.UserMyPageResponse
import com.backend.api.user.service.UserMyPageService
import com.backend.domain.user.entity.User
import com.backend.domain.user.repository.UserRepository
import com.backend.global.Rq.Rq
import com.backend.global.dto.response.ApiResponse
import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/users")
@Tag(name = "Users", description = "마이페이지 관련 API")
class UserMyPageController(
    private val postService: PostService,
    private val userMyPageService: UserMyPageService,
    private val rq: Rq,
    private val commentService: CommentService,
    private val answerService: AnswerService,
    private val questionService: QuestionService,
    private val userRepository: UserRepository
){

    private fun currentUser(): User = rq.getUser()

    @GetMapping("/me")
    @Operation(summary = "개인 정보 조회")
    fun detailInformation(): ApiResponse<UserMyPageResponse> {
        val user = runCatching { currentUser() }
            .getOrElse { throw ErrorException(ErrorCode.UNAUTHORIZED_USER) }

        return ApiResponse.ok(userMyPageService.getInformation(user.id))
    }

    @PutMapping("/me")
    @Operation(summary = "개인 정보 수정")
    fun updateUser(
        @RequestBody modify: MyPageRequest.UserModify
    ): ApiResponse<UserMyPageResponse> {
        val userId = currentUser().id
        val response = userMyPageService.modifyUser(userId, modify)

        return ApiResponse.ok("개인 정보 수정이 완료되었습니다.", response)
    }

    @GetMapping("/{userId}")
    @Operation(summary = "마이페이지 상세 정보 조회")
    fun detailInformation(@PathVariable userId: Long): ApiResponse<UserMyPageResponse> {
        val response = userMyPageService.getInformation(userId)

        return ApiResponse.ok("사용자 상세 정보 조회를 완료했습니다.", response)
    }

    @GetMapping("/{userId}/posts")
    @Operation(summary = "사용자가 작성한 모집글 목록 조회")
    fun getUserPosts(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<PostPageResponse<PostResponse>> {
        val userPostsPage = postService.getPostsByUserId(page, userId)

        return ApiResponse.ok(
            "사용자가 작성한 모집글 목록 조회를 완료했습니다.",
            userPostsPage
        )
    }

    @GetMapping("/{userId}/questions")
    @Operation(summary = "사용자가 작성한 질문 목록 조회")
    fun getUserQuestions(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<QuestionPageResponse<QuestionResponse>> {
        val userQuestionsPage =
            questionService.findQuestionsByUserId(page, userId)

        return ApiResponse.ok(
            "사용자가 작성한 질문 목록 조회를 완료했습니다.",
            userQuestionsPage
        )
    }

    @GetMapping("/{userId}/comments")
    @Operation(summary = "사용자가 작성한 댓글 목록 조회")
    fun getUserComments(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<CommentPageResponse<CommentMypageResponse>> {
        val userCommentsPage = commentService.getCommentsByUserId(page, userId)

        return ApiResponse.ok(
            "사용자가 작성한 댓글 목록 조회를 완료했습니다.",
            userCommentsPage
        )
    }


    @GetMapping
    @Operation(summary = "해결한 문제")
    fun solvedProblemList(): ApiResponse<List<UserMyPageResponse.SolvedProblem>> {
        val userId = currentUser().id
        val solvedList = userMyPageService.getSolvedProblems(userId)
        return ApiResponse.ok(solvedList)
    }


    @GetMapping("/{userId}/answers")
    @Operation(summary = "사용자가 작성한 면접 답변 목록 조회")
    fun getUserAnswers(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "1") page: Int
    ): ApiResponse<AnswerPageResponse<AnswerMypageResponse>> {
        val userAnswersPage =
            answerService.findAnswersByUserId(page, userId)

        return ApiResponse.ok(
            "사용자가 작성한 면접 답변 목록 조회를 완료했습니다.",
            userAnswersPage
        )
    }

    @PostMapping("/verifyPassword")
    @Operation(summary = "비밀번호 확인")
    fun verifyPassword(@RequestBody requestBody: Map<String, String>): ApiResponse<Boolean> {
        val isTraditionalLoginUser = currentUser().oauthId == null
        val userId = currentUser().id
        val inputPassword = requestBody["password"]
        val isValid = userMyPageService.verifyPassword(userId, inputPassword)

        if (!isTraditionalLoginUser) {
            // 이메일 가입 유저가 아닐 경우 커스텀 예외 발생
            throw ErrorException(ErrorCode.INVALID_AUTHENTICATION_SNS)
        }
        else if(!isValid) {
            // 비밀번호가 틀릴 경우 커스텀 예외 발생
            throw ErrorException(ErrorCode.WRONG_PASSWORD)
        }
        
        return ApiResponse.ok("비밀번호가 확인되었습니다.", true)
    }

    @PostMapping("/verifyOauthId")
    @Operation(summary = "SNS 로그인 정보 확인")
    fun verifyOauthId(@RequestBody requestBody: Map<String, String>): ApiResponse<Boolean> {
        val isSNSLoginUser = currentUser().oauthId != null
        val oauthId = requestBody["oauthId"] ?: throw ErrorException(ErrorCode.INVALID_OAUTHID)
        val isValid = oauthId == currentUser().oauthId

        if (!isSNSLoginUser) {
            // 소셜 로그인 유저가 아닐 경우 커스텀 예외 발생
            throw ErrorException(ErrorCode.INVALID_AUTHENTICATION_EMAIL)
        }
        else if(!isValid) {
            // 현재 로그인한 유저와 SNS 인증 정보가 다를 경우 예외 발생
            throw ErrorException(ErrorCode.FORBIDDEN_USER)
        }

        return ApiResponse.ok("SNS 인증이 확인되었습니다.", true)
    }

    @GetMapping("/{userId}/questions/{questionId}")
    @Operation(summary = "사용자가 작성한 승인되지 않은 질문 단건 조회 (수정용)")
    fun getUserQuestionForEdit(
        @PathVariable userId: Long,
        @PathVariable questionId: Long
    ): ApiResponse<QuestionResponse> {
        val currentUser = rq.getUser()
        val response = questionService.getNotApprovedQuestionById(userId, questionId, currentUser)

        return ApiResponse.ok("사용자가 작성한 승인되지 않은 질문 단건 조회를 완료했습니다.", response)
    }
}
