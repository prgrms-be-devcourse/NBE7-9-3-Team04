package com.backend.global.dto.response

import com.backend.global.exception.ErrorCode
import com.backend.global.exception.ErrorException
import org.springframework.http.HttpStatus

@JvmRecord
data class ApiResponse<T>(
    val status: HttpStatus,
    val message: String?,
    val data: T?
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> =
            ApiResponse(HttpStatus.OK, "유저 정보를 불러왔습니다.", data)

        fun <T> ok(message: String, data: T): ApiResponse<T> =
            ApiResponse(HttpStatus.OK, message, data)

        fun <T> created(message: String, data: T): ApiResponse<T> =
            ApiResponse(
                status = HttpStatus.CREATED,
                message = message,
                data = data
            )

        @JvmStatic
        fun noContent(message: String): ApiResponse<Unit> =
            ApiResponse(
                status = HttpStatus.NO_CONTENT,
                message = message,
                data = null
            )

        fun fail(errorException: ErrorException): ApiResponse<Unit> {
            val errorCode: ErrorCode = errorException.errorCode
            return ApiResponse(
                status = errorCode.httpStatus,
                message = errorCode.message,
                data = null
            )
        }

        fun fail(status: HttpStatus, message: String): ApiResponse<Unit> =
            ApiResponse(
                status = status,
                message = message,
                data = null
            )
    }
}
