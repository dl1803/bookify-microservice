package com.dl1803.identity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error!", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key!", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed!", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed!", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission!", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1009, "Invalid email address", HttpStatus.BAD_REQUEST),
    EMAIL_IS_REQUIRED(1009, "Email is required", HttpStatus.BAD_REQUEST),
    INVALID_OTP(1033, "Invalid OTP code", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1034, "OTP has expired", HttpStatus.BAD_REQUEST),
    USER_NOT_VERIFIED(1035, "Email is not verified", HttpStatus.FORBIDDEN),
    USER_ALREADY_VERIFIED(1036, "Email is already verified", HttpStatus.BAD_REQUEST),
    OTP_SEND_COOLDOWN(1037, "Please wait before requesting a new OTP", HttpStatus.TOO_MANY_REQUESTS),
    TOO_MANY_OTP_ATTEMPTS(1038, "Too many failed attempts. Please request a new OTP", HttpStatus.BAD_REQUEST),
    INVALID_GOOGLE_TOKEN(1039, "Invalid Google token", HttpStatus.BAD_REQUEST),
    GOOGLE_AUTH_FAILED(1040, "Google authentication failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_LINKED_TO_DIFFERENT_PROVIDER(
            1041,
            "This email is already registered with a different login method",
            HttpStatus.CONFLICT); // login local khi đã login GG

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
