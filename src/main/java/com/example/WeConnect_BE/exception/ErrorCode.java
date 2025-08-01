package com.example.WeConnect_BE.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNAUTHORIZED(402, "Unauthorized", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_USER_NOT_FOUND(404, "Email User Not Found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(400, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(404, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(400, "Username must be at least 3 characters long", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(400, "Passwords must be at least 8 characters long", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTS(404, "User not exists", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND(404, "File Not Found", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(401, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(403, "you do not have permission", HttpStatus.FORBIDDEN),
    OTP_NOT_FOUND_OR_EXPIRED(404, "Otp Not Found", HttpStatus.NOT_FOUND),
    INVALID_OTP(400, "Invalid Otp", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(400, "Otp Expired", HttpStatus.BAD_REQUEST),

    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private String message;
    private int code;
    private HttpStatusCode statusCode;


}