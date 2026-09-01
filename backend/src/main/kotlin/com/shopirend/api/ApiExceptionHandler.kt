package com.shopirend.api

import com.shopirend.service.ApiException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

data class ErrorResponse(val status: Int, val message: String, val timestamp: Instant = Instant.now())

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun api(exception: ApiException) = ResponseEntity.status(exception.status).body(ErrorResponse(exception.status.value(), exception.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(exception: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = exception.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), message))
    }
}
