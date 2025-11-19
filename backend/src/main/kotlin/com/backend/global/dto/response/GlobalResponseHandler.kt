package com.backend.global.dto.response

import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice
class GlobalResponseHandler : ResponseBodyAdvice<Any?> {
    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>?>
    ): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>?>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        // 혹시 모를 ErrorResponse 처리를 위해 -> 커스텀 예외처리가 아닐 경우

        var responseBody = body
        if (responseBody is ErrorResponse) {
            responseBody = responseBody.body
        }

        if (responseBody is ApiResponse<*>) {
            response.setStatusCode(responseBody.status)
        }
        return responseBody
    }
}
