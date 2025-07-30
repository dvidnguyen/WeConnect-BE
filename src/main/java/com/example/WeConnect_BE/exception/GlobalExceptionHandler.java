package com.example.WeConnect_BE.exception;

import com.example.WeConnect_BE.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(Exception ex) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNAUTHORIZED.getCode());
        apiResponse.setMessage(ErrorCode.UNAUTHORIZED.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse> handleAppException(AppException ex) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ex.getError().getCode());
        apiResponse.setMessage(ex.getMessage());
        return ResponseEntity
                .status(ex.getError().getStatusCode())
                .body(apiResponse);
    }
    @ExceptionHandler(value = AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse> handleMethodAccessDeniedException(AuthorizationDeniedException ex) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.ACCESS_DENIED.getCode());
        apiResponse.setMessage(ErrorCode.ACCESS_DENIED.getMessage());

        return ResponseEntity
                .status(ErrorCode.ACCESS_DENIED.getStatusCode())
                .body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorCode = ex.getFieldError().getDefaultMessage();
        ErrorCode  error = ErrorCode.valueOf(errorCode);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(error.getCode());
        apiResponse.setMessage(error.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

}
