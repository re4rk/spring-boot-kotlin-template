package io.dodn.springboot.core.support.error

import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpStatus

enum class ErrorType(val status: HttpStatus, val code: ErrorCode, val message: String, val logLevel: LogLevel) {
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "An unexpected error has occurred.", LogLevel.ERROR),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Invalid payment amount.", LogLevel.WARN),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Payment processing failed.", LogLevel.ERROR),
    EXTERNAL_PAYMENT_FAILED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "External payment processing failed.", LogLevel.ERROR),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "Payment not found.", LogLevel.WARN),
    EXTERNAL_PAYMENT_ID_NOT_FOUND(HttpStatus.BAD_REQUEST, ErrorCode.E400, "External payment ID not found.", LogLevel.WARN),
}