package io.dodn.springboot.core.support.error

import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpStatus

enum class ErrorType(val status: HttpStatus, val code: ErrorCode, val message: String, val logLevel: LogLevel) {
    // System Errors
    DEFAULT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.E500, "An unexpected error has occurred.", LogLevel.ERROR),

    // Parameter Errors
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Invalid parameter", LogLevel.WARN),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Validation error", LogLevel.WARN),

    // Authentication & Authorization Errors
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "Unauthorized", LogLevel.WARN),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, ErrorCode.E401, "Invalid email or password", LogLevel.WARN),

    // Payment Related Errors
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Invalid payment amount.", LogLevel.WARN),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Payment processing failed.", LogLevel.ERROR),
    EXTERNAL_PAYMENT_FAILED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "External payment processing failed.", LogLevel.ERROR),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "Payment not found.", LogLevel.WARN),
    EXTERNAL_PAYMENT_ID_NOT_FOUND(HttpStatus.BAD_REQUEST, ErrorCode.E400, "External payment ID not found.", LogLevel.WARN),

    // User Account Management Errors
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "User not found", LogLevel.WARN),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, ErrorCode.E409, "Email already exists", LogLevel.WARN),
    USER_INACTIVE(HttpStatus.FORBIDDEN, ErrorCode.E403, "User account is not active", LogLevel.WARN),
    USER_NOT_DELETED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is not deleted", LogLevel.WARN),
    USER_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is already verified", LogLevel.INFO),
    USER_ALREADY_LOCKED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is already locked", LogLevel.INFO),
    USER_ALREADY_ACTIVE(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is already active", LogLevel.INFO),
    USER_ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is already inactive", LogLevel.INFO),
    USER_NOT_LOCKED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User is not locked", LogLevel.WARN),

    // Password & Security Errors
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Password is too weak", LogLevel.WARN),
    PASSWORD_REUSED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Password has been used before", LogLevel.WARN),

    // Token Related Errors
    EXPIRED_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Expired verification token", LogLevel.WARN),
    INVALID_VERIFICATION_TOKEN(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Invalid verification token", LogLevel.INFO),
    USED_RESET_TOKEN(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Used reset token", LogLevel.WARN),
    EXPIRED_RESET_TOKEN(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Expired reset token", LogLevel.WARN),
    INVALID_RESET_TOKEN(HttpStatus.BAD_REQUEST, ErrorCode.E400, "Invalid reset token", LogLevel.WARN),

    //  Data Integrity Errors
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, ErrorCode.E409, "Concurrent modification detected", LogLevel.WARN),

    // Feed Related Errors
    FEED_PERMISSION_DENIED(HttpStatus.FORBIDDEN, ErrorCode.E403, "User does not have permission to share this worry", LogLevel.WARN),
    FEED_ALREADY_EMPATHIZED(HttpStatus.BAD_REQUEST, ErrorCode.E400, "User has already empathized with this feed", LogLevel.WARN),

    // Worry Related Errors
    WORRY_PERMISSION_DENIED(HttpStatus.FORBIDDEN, ErrorCode.E403, "User does not have permission to access this worry", LogLevel.WARN),
    WORRY_NOT_FOUND(HttpStatus.NOT_FOUND, ErrorCode.E404, "Worry not found", LogLevel.WARN),
}
