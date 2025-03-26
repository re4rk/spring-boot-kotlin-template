package io.dodn.springboot.core.support.error

import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpStatus

enum class ErrorType(val status: HttpStatus, val code: ErrorCode, val message: String, val logLevel: LogLevel) {
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "An unexpected error has occurred.", LogLevel.ERROR),

// Payment
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Invalid payment amount.", LogLevel.WARN),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Payment processing failed.", LogLevel.ERROR),
    EXTERNAL_PAYMENT_FAILED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "External payment processing failed.", LogLevel.ERROR),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "Payment not found.", LogLevel.WARN),
    EXTERNAL_PAYMENT_ID_NOT_FOUND(HttpStatus.BAD_REQUEST, ErrorCode.E400, "External payment ID not found.", LogLevel.WARN),

// User
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "Unauthorized", LogLevel.WARN),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "User not found", LogLevel.WARN),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "Invalid email or password", LogLevel.WARN),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, ErrorCode.E409, "Email already exists", LogLevel.WARN),
    USER_INACTIVE(HttpStatus.FORBIDDEN, ErrorCode.E403, "User account is not active", LogLevel.WARN),
    EXPIRED_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Expired verification token", LogLevel.WARN),
    INVALID_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Invalid verification token", LogLevel.INFO),
    USER_NOT_DELETED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is not deleted", LogLevel.WARN),
    USER_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is already verified", LogLevel.INFO),
    USER_ALREADY_LOCKED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is already locked", LogLevel.INFO),
    USER_ALREADY_ACTIVE(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is already active", LogLevel.INFO),
    USER_ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is already inactive", LogLevel.INFO),
}
