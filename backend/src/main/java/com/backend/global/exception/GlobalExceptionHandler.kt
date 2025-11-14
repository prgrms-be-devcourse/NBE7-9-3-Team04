package com.backend.global.exception

import com.backend.global.dto.response.ApiResponse
import com.fasterxml.jackson.core.JsonProcessingException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    // 커스텀 예외 처리
    @ExceptionHandler(ErrorException::class)
    protected fun handleCustomException(e: ErrorException): ResponseEntity<ApiResponse<*>?> {
        logger.error("ErrorException: ${e.errorCode.name} - ${e.message}")
        return ResponseEntity<ApiResponse<*>?>(
            ApiResponse.fail(e),
            e.errorCode.httpStatus
        )
    }

    // @Valid 유효성 검사 실패 시 발생하는 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<*>?> {
        val message = e.getBindingResult().getFieldError()!!.getDefaultMessage()
        logger.warn("MethodArgumentNotValidException ${e.message}")
        return ResponseEntity<ApiResponse<*>?>(
            ApiResponse.fail(HttpStatus.BAD_REQUEST, message),
            HttpStatus.BAD_REQUEST
        )
    }

    // JSON 직렬화/역직렬화 예외
    @ExceptionHandler(JsonProcessingException::class)
    fun handleJsonProcessing(e: JsonProcessingException): ResponseEntity<ApiResponse<*>?> {
        logger.error("JSON 파싱 실패: ${e.message}")
        return ResponseEntity<ApiResponse<*>?>(
            ApiResponse.fail(HttpStatus.BAD_REQUEST, e.message),
            HttpStatus.BAD_REQUEST
        )
    }


    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception::class)
    fun handleAllException(e: Exception): ResponseEntity<ApiResponse<*>?> {
        logger.error("handleAllException ${e.message}")
        return ResponseEntity<ApiResponse<*>?>(
            ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, e.message),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
