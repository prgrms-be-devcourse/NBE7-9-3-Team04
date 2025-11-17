package com.backend.global.dto.response

import com.backend.global.exception.ErrorException
import org.springframework.http.HttpStatus


data class ApiResponse<T>(
    val status: HttpStatus,
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> ok(message: String, data: T?): ApiResponse<T> {
            return ApiResponse(HttpStatus.OK, message, data)
        }

        fun <T> ok(data: T?): ApiResponse<T> = ApiResponse(HttpStatus.OK, "요청이 성공적으로 처리되었습니다.", data)

        fun <T> created(message: String, data: T?): ApiResponse<T> = ApiResponse(HttpStatus.CREATED, message, data)

        fun <T> noContent(message: String): ApiResponse<T> = ApiResponse(HttpStatus.NO_CONTENT, message, null)

        fun fail(errorException: ErrorException): ApiResponse<*> {
            val errorCode = errorException.errorCode
            return ApiResponse(
                errorCode.httpStatus,
                errorCode.message,
                null
            )
        }

        fun fail(status: HttpStatus, message: String?): ApiResponse<*> {
            return ApiResponse(
                status,
                message ?: "메시지가 비어있습니다.",
                null
            )
        }
    }
}