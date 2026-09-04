package com.dl1803.profile.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error!", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key!", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed!", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission!", HttpStatus.FORBIDDEN),
    // === Friend System Error Codes ===
    SELF_ACTION_NOT_ALLOWED(1017, "You cannot perform this action on yourself!", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_EXISTED(1018, "Friend request has already been sent", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_NOT_FOUND(1019, "Friend request not found", HttpStatus.NOT_FOUND),
    ALREADY_FRIENDS(1020, "Users are already friends", HttpStatus.BAD_REQUEST),
    NOT_FRIEND(1021, "Users are not friends", HttpStatus.BAD_REQUEST),
    NOT_FRIEND_REQUEST_RECIPIENT(1022, "You are not the recipient of this friend request", HttpStatus.FORBIDDEN),
    NOT_FRIEND_REQUEST_SENDER(1023, "You are not the sender of this friend request", HttpStatus.FORBIDDEN),
    // === Block System Error Codes ===
    USER_ALREADY_BLOCKED(1026, "User is already blocked", HttpStatus.BAD_REQUEST),
    USER_BLOCKED(1027, "Action cannot be performed because of block relationship", HttpStatus.BAD_REQUEST),
    USER_NOT_BLOCKED(1028, "User is not currently blocked", HttpStatus.BAD_REQUEST),
    // === Report System Error Codes ===
    CANNOT_REPORT_SELF(1029, "You cannot report yourself", HttpStatus.BAD_REQUEST),
    ALREADY_REPORTED(1030, "You have already reported this user", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
