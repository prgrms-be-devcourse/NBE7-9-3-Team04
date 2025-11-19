package com.backend.api.user.controller.testsupport

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


class UnauthenticatedException(message: String = "로그인된 사용자가 없습니다.") : RuntimeException(message)

@ControllerAdvice
class TestAuthExceptionHandler {

    data class ErrorResponse(val status: String, val message: String)

    @ExceptionHandler(UnauthenticatedException::class)
    fun handleUnauthenticated(ex: UnauthenticatedException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(status = "UNAUTHORIZED", message = ex.message ?: "로그인된 사용자가 없습니다.")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)
    }
}