package com.backend.api.answer.controller;

import com.backend.api.answer.dto.request.AnswerCreateRequest;
import com.backend.api.answer.dto.request.AnswerUpdateRequest;
import com.backend.api.answer.dto.response.*;
import com.backend.api.answer.service.AnswerService;
import com.backend.domain.answer.entity.Answer;
import com.backend.domain.user.entity.User;
import com.backend.global.Rq.Rq;
import com.backend.global.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/questions")
@Tag(name = "AnswerController", description = "면접 답변 API")
public class AnswerController {

    private final Rq rq;
    private final AnswerService answerService;

    @PostMapping("/{questionId}/answers")
    @Operation(summary = "답변 작성")
    public ApiResponse<AnswerCreateResponse> createAnswer(
            @PathVariable Long questionId,
            @RequestBody @Valid AnswerCreateRequest reqBody
    ) {
        User currentUser = rq.getUser();

        Answer newAnswer = answerService.writeAnswer(currentUser, questionId, reqBody);

        return ApiResponse.created(
                "%d번 답변이 생성되었습니다.".formatted(newAnswer.getId()),
                new AnswerCreateResponse(newAnswer)
        );
    }

    @PatchMapping("/{questionId}/answers/{answerId}")
    @Operation(summary = "답변 수정")
    public ApiResponse<AnswerUpdateResponse> updateAnswer(
            @PathVariable Long answerId,
            @RequestBody @Valid AnswerUpdateRequest reqBody
    ) {
        User currentUser = rq.getUser();

        Answer updatedAnswer = answerService.updateAnswer(
                currentUser,
                answerId,
                reqBody
        );

        return ApiResponse.ok(
                "%d번 답변이 수정되었습니다.".formatted(updatedAnswer.getId()),
                new AnswerUpdateResponse(updatedAnswer)
        );
    }

    @DeleteMapping("/{questionId}/answers/{answerId}")
    @Operation(summary = "답변 삭제")
    public ApiResponse<Void> deleteAnswer(
            @PathVariable Long answerId
    ) {
        User currentUser = rq.getUser();

        answerService.deleteAnswer(currentUser, answerId);

        return ApiResponse.ok(
                "%d번 답변이 삭제되었습니다.".formatted(answerId),
                null
        );
    }

    @GetMapping("/{questionId}/answers")
    @Operation(summary = "답변 목록 조회")
    public ApiResponse<AnswerPageResponse<AnswerReadWithScoreResponse>> readAnswers(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "1") int page
    ) {
        AnswerPageResponse<AnswerReadWithScoreResponse> answersPage = answerService.findAnswersByQuestionId(page, questionId);

        return ApiResponse.ok(
                "%d번 질문의 답변 목록 조회 성공".formatted(questionId),
                answersPage
        );
    }

    @GetMapping(value = "/{questionId}/answers/mine")
    @Transactional(readOnly = true)
    @Operation(summary = "내 답변 조회")
    public ApiResponse<AnswerReadResponse> readMyAnswer(
            @PathVariable Long questionId
    ) {

        AnswerReadResponse answerResponse = answerService.findMyAnswer(questionId);

        return ApiResponse.ok(
                "%d번 질문의 내 답변 조회 성공".formatted(questionId),
                answerResponse
        );
    }

}
