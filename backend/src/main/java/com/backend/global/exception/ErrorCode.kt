package com.backend.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val message: String
) {
    //사용자 관련
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    NOT_FOUND_NICKNAME(HttpStatus.NOT_FOUND, "작성자를 찾을 수 없습니다."),
    NOT_FOUND_EMAIL(HttpStatus.NOT_FOUND, "이메일이 존재하지 않습니다."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "로그인된 사용자가 없습니다."),
    USER_STATUS_NOT_BLANK(HttpStatus.BAD_REQUEST, "유저 상태는 공백일 수 없습니다."),
    DUPLICATE_STATUS(HttpStatus.BAD_REQUEST, "동일한 상태로 변경할 수 없습니다."),
    INVALID_USER_ROLE(HttpStatus.FORBIDDEN, "권한이 없는 사용자입니다."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "정지 상태인 계정입니다."),
    ACCOUNT_DEACTIVATED(HttpStatus.FORBIDDEN, "비활성화된 계정입니다."),
    ACCOUNT_BANNED(HttpStatus.FORBIDDEN, "영구 정지된 계정입니다."),
    SELF_INFORMATION(HttpStatus.UNAUTHORIZED, "본인 정보만 수정할 수 있습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),
    EXPIRED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),
    USER_QUESTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 유저-문제 관계입니다."),
    INVALID_SUSPEND_REASON(HttpStatus.BAD_REQUEST, "정지 사유가 필요합니다."),
    INVALID_SUSPEND_PERIOD(HttpStatus.BAD_REQUEST, "유효한 정지 종료일을 입력해주세요."),
    INVALID_BAN_PERIOD(HttpStatus.BAD_REQUEST, "영구 정지 상태에서는 종료일을 지정할 수 없습니다."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 상태 값입니다."),

    //token
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh Token을 찾을 수 없습니다."),

    //Category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리입니다."),


    // Post & Comment
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    COMMENT_INVALID_USER(HttpStatus.FORBIDDEN, "해당 댓글에 대한 권한이 없는 사용자입니다."),
    PIN_POST_FORBIDDEN(HttpStatus.FORBIDDEN, "게시물 상단 고정 권한이 없습니다."),
    INVALID_DEADLINE(HttpStatus.BAD_REQUEST, "마감일은 현재 시간 이후로 설정해야 합니다."),

    //Qna
    QNA_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 Q&A를 찾을 수 없습니다."),
    QNA_ACCESS_DENIED(HttpStatus.FORBIDDEN, "자신의 문의만 수정하거나 삭제할 수 있습니다."),
    QNA_ALREADY_ANSWERED(HttpStatus.BAD_REQUEST, "이미 답변이 등록된 Q&A입니다."),
    QNA_CATEGORY_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리입니다."),
    QNA_CONTENT_NOT_BLANK(HttpStatus.BAD_REQUEST, "문의 내용은 비워둘 수 없습니다."),
    QNA_TITLE_NOT_BLANK(HttpStatus.BAD_REQUEST, "문의 제목은 비워둘 수 없습니다."),
    QNA_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "Q&A 답변 등록 권한이 없습니다."),
    QNA_ADMIN_ANSWER_FINISHED(HttpStatus.BAD_REQUEST, "관리자가 답변을 완료한 Q&A입니다."),

    //Question
    QUESTION_TITLE_NOT_BLANK(HttpStatus.BAD_REQUEST, "질문 제목은 공백일 수 없습니다."),
    QUESTION_CONTENT_NOT_BLANK(HttpStatus.BAD_REQUEST, "질문 내용은 공백일 수 없습니다."),
    NOT_FOUND_QUESTION(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다."),
    INVALID_QUESTION_SCORE(HttpStatus.BAD_REQUEST, "질문 점수는 음수일 수 없습니다."),
    QUESTION_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 질문입니다."),
    ALREADY_APPROVED_QUESTION(HttpStatus.BAD_REQUEST, "이미 승인된 질문입니다."),
    QUESTION_NOT_APPROVED(HttpStatus.FORBIDDEN, "승인되지 않은 질문입니다."),
    NOT_FOUND_CONTENT(HttpStatus.NOT_FOUND, "질문 내용을 찾을 수 없습니다."),
    AI_QUESTION_LIMIT_EXCEEDED(HttpStatus.FORBIDDEN, "정해진 AI 질문 횟수를 모두 사용했습니다."),
    QUESTION_INVALID_USER(HttpStatus.FORBIDDEN, "해당 질문에 대한 권한이 없는 사용자입니다."),

    // Answer
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 답변입니다."),
    ANSWER_NOT_PUBLIC(HttpStatus.FORBIDDEN, "비공개 답변입니다."),
    ANSWER_INVALID_USER(HttpStatus.FORBIDDEN, "해당 답변에 대한 권한이 없는 사용자입니다."),

    // resume
    DUPLICATE_RESUME(HttpStatus.BAD_REQUEST, "이미 등록된 이력서가 있습니다."),
    NOT_FOUND_RESUME(HttpStatus.NOT_FOUND, "이력서를 찾을 수 없습니다."),
    INVALID_USER(HttpStatus.FORBIDDEN, "이력서 수정 권한이 없습니다."),
    AI_FEEDBACK_FOR_PREMIUM_ONLY(HttpStatus.FORBIDDEN, "포트폴리오 첨삭은 PREMIUM 등급 사용자만 이용 가능합니다."),
    NOT_FOUND_REVIEW(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다"),
    ACCESS_DENIED_REVIEW(HttpStatus.FORBIDDEN, "해당 리뷰에 접근할 권한이 없습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 파라미터입니다."),


    //payment
    PAYMENT_APPROVE_FAILED(HttpStatus.BAD_REQUEST, "결제 승인을 실패했습니다."),
    PAYMENT_LOAD_FAILED(HttpStatus.BAD_REQUEST, "결제 조회를 실패했습니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_REQUEST, "결제 취소를 실패했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 내역을 찾을 수 없습니다."),
    AUTO_PAYMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "자동 결제 처리 중 오류가 발생했습니다."),

    //billing
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 고객의 구독 정보를 찾을 수 없습니다."),
    SUBSCRIPTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 구독 중입니다."),
    SUBSCRIPTION_INACTIVE(HttpStatus.BAD_REQUEST, "비활성화된 구독입니다."),
    BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "빌링키를 찾을 수 없습니다."),
    INVALID_AUTH_KEY(HttpStatus.BAD_REQUEST, "AUTH_KEY가 누락되었거나 유효하지 않습니다."),
    INVALID_CUSTOMER_KEY(HttpStatus.BAD_REQUEST, "CUSTOMER_KEY가 누락되었거나 유효하지 않습니다."),
    BILLING_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "빌링키 발급 응답을 받지 못했습니다"),


    // feedback
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "피드백을 찾을 수 없습니다."),
    FETCH_FEEDBACK_FAILED(HttpStatus.BAD_REQUEST, "AI 피드백 조회에 실패했습니다."),

    //ranking
    RANKING_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 랭킹입니다."),
    RANKING_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자의 랭킹 정보를 찾을 수 없습니다."),
    INVALID_SCORE(HttpStatus.BAD_REQUEST, "유효하지 않은 점수 값입니다."),
    RANKING_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "랭킹 정보를 사용할 수 없습니다."),

    REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 처리 중 오류가 발생했습니다."),
    ;

}
