package com.backend.global.dto.response;

import com.backend.global.exception.ErrorCode;
import com.backend.global.exception.ErrorException;
import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        HttpStatus status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok( String message, T data) {
        return new ApiResponse<>(HttpStatus.OK, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok("유저 정보를 불러왔습니다.", data);
    }

    public static <T> ApiResponse<T> created( String message, T data) {
        return new ApiResponse<>(HttpStatus.CREATED, message, data);
    }

    public static <T> ApiResponse<T> noContent( String message) {
        return new ApiResponse<>(HttpStatus.NO_CONTENT, message, null);
    }

    public static ApiResponse<?> fail(ErrorException errorException) {
        ErrorCode errorCode = errorException.getErrorCode();
        return new ApiResponse<>(
                errorCode.getHttpStatus(),
                errorCode.getMessage(),
                null
        );
    }

    public static ApiResponse<?> fail(HttpStatus status, String message) {
        return new ApiResponse<>(
                status,
                message,
                null
        );
    }
}