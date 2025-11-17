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

        fun <T> created(message: String, data: T?): ApiResponse<T> = ApiResponse(HttpStatus.CREATED, message, data)

        fun <T> noContent(message: String): ApiResponse<T> = ApiResponse(HttpStatus.NO_CONTENT, message, null)

        fun <T> fail(errorException: ErrorException): ApiResponse<T> {
            val errorCode = errorException.errorCode
            return ApiResponse(
                errorCode.httpStatus,
                errorCode.message,
                null
            )
        }

        fun <T> fail(status: HttpStatus, message: String): ApiResponse<T> {
            return ApiResponse(
                status,
                message,
                null
            )
        }
    }
}