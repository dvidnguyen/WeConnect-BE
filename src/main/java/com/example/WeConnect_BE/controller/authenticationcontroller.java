package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.request.AuthenticationRequest;
import com.example.WeConnect_BE.dto.request.RegisterRequest;
import com.example.WeConnect_BE.dto.response.AuthenticationResponse;
import com.example.WeConnect_BE.dto.response.RegisterReponse;
import com.example.WeConnect_BE.entity.InvalidToken;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.repository.InvalidTokenRepository;
import com.example.WeConnect_BE.repository.UserSessionRepository;
import com.example.WeConnect_BE.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class authenticationcontroller {
    AuthenticationService authenticationService;


    @PostMapping("/user")
    public ApiResponse<List<User>> getuser() throws ParseException, JOSEException {
        return ApiResponse.<List<User>>builder()
                .result(authenticationService.getUser())
                .build();
    }


    @PostMapping("/regis")
    public ApiResponse<RegisterReponse> regis(@RequestBody RegisterRequest request) throws ParseException, JOSEException {
        return ApiResponse.<RegisterReponse>builder()
                .result(authenticationService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            AuthenticationResponse response = authenticationService.login(authenticationRequest);

            return ApiResponse.<String>builder()
                    .code(200)
                    .message("Login successful")
                    .result(response.getToken())
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .code(401)
                    .message("Login failed: " + e.getMessage())
                    .build();
        }
    }



    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String token) {
        try {
            // Gọi service logout
            authenticationService.logout(token);

            // Trả về ApiResponse khi logout thành công
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("Logout successful")
                    .build();

        } catch (AppException e) {
            // Nếu có lỗi, trả về thông báo lỗi
            return ApiResponse.<String>builder()
                    .code(401)
                    .message("Logout failed: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            // Nếu có lỗi không xác định
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("Logout failed: " + e.getMessage())
                    .build();
        }
    }

}