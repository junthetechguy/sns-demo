package com.fastcampus.sns.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode { // TDD로 Test Code를 먼저 작성하면서 발생할 수 있는 ErrorCode들을 미리 Enum class로 모아서 정의해둔 것.
    DUPLICATED_USER_NAME(HttpStatus.CONFLICT, "User name is duplicated"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Password is invalid"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token is invalid"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "Post not found"),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "Permission is invalid"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurs in database"),
    ALREADY_LIKED(HttpStatus.CONFLICT, "User already liked the post"),
    ALARM_CONNECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Connecting alarm occurs error")
    ;

    // 각각의 ErrorCode가 가지는 Field 선언
    private HttpStatus status;
    private String message;
}