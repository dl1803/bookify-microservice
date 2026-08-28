package com.dl1803.identity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(
            9999, "An unexpected error occurred. Please try again later", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key!", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "Username or email is already taken", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not found", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Authentication is required", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission to access this resource", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1009, "Please enter a valid email address", HttpStatus.BAD_REQUEST),
    EMAIL_IS_REQUIRED(1048, "Email address is required", HttpStatus.BAD_REQUEST),

    // === Auth-specific error codes ===
    INVALID_CREDENTIALS(1042, "Username or password is incorrect", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1043, "Your session has expired. Please login again", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOKED(1044, "Your session has been revoked. Please login again", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(1045, "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    WRONG_CURRENT_PASSWORD(1046, "Current password is incorrect", HttpStatus.BAD_REQUEST),
    PASSWORD_CHANGED_RELOGIN(1047, "Password has been changed. Please login again", HttpStatus.UNAUTHORIZED),

    // === OTP & Verification ===
    INVALID_OTP(1033, "Invalid OTP code", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1034, "OTP has expired. Please request a new one", HttpStatus.BAD_REQUEST),
    USER_NOT_VERIFIED(1035, "Email is not verified. Please check your inbox", HttpStatus.FORBIDDEN),
    USER_ALREADY_VERIFIED(1036, "Email is already verified", HttpStatus.BAD_REQUEST),
    OTP_SEND_COOLDOWN(1037, "Please wait before requesting a new OTP", HttpStatus.TOO_MANY_REQUESTS),
    TOO_MANY_OTP_ATTEMPTS(1038, "Too many failed attempts. Please request a new OTP", HttpStatus.BAD_REQUEST),

    // === Google Auth ===
    INVALID_GOOGLE_TOKEN(1039, "Google authentication failed. Please try again", HttpStatus.BAD_REQUEST),
    GOOGLE_AUTH_FAILED(1040, "Unable to connect to Google. Please try again later", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_LINKED_TO_DIFFERENT_PROVIDER(
            1041, "This email is already registered with a different login method", HttpStatus.CONFLICT);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
