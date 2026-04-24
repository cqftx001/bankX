package com.bankx.demo.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    SUCCESS("0000", "Success", HttpStatus.OK),

    INVALID_REQUEST("1000", "Invalid request", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("1001", "Validation failed", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("1002", "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("1003", "Forbidden", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND("1004", "Resource not found", HttpStatus.NOT_FOUND),
    DUPLICATE_REQUEST("1005", "Duplicate request", HttpStatus.CONFLICT),
    OPTIMISTIC_LOCK_CONFLICT("1006", "Optimistic lock conflict", HttpStatus.CONFLICT),

    USER_NOT_FOUND("2001", "User not found", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND("2002", "Account not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_FUNDS("2003", "Insufficient funds", HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR("9999", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),


    // Email Service
    TOO_MANY_REQUESTS("3000", "Too many requests", HttpStatus.TOO_MANY_REQUESTS),
    WRONG_VERIFICATION_CODE("3001", "Wrong verification code", HttpStatus.BAD_REQUEST);

    //

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}