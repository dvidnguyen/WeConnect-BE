package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.Util.OtpUtil;
import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.request.OtpRequest;
import com.example.WeConnect_BE.dto.request.VerifyOtpRequest;
import com.example.WeConnect_BE.dto.response.OtpResponse;
import com.example.WeConnect_BE.dto.response.VerifyOtpResponse;
import com.example.WeConnect_BE.repository.UserRepository;
import com.example.WeConnect_BE.service.AuthenticationService;
import com.example.WeConnect_BE.service.SendMailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SendMailController {
    SendMailService sendMailService;
    AuthenticationService authenticationService;

    @PostMapping("/send")
    public ApiResponse<OtpResponse> sendOtp(@RequestBody OtpRequest request) {
        boolean valid = true;
        try {
            String otp = OtpUtil.generateOtp(6); // 6 chữ số
            // Gửi mail
            sendMailService.sendOtpEmail(request.getEmail(), otp);
        } catch (Exception e) {
            valid = false;
        }

        return ApiResponse.<OtpResponse>builder()
                .result(OtpResponse.builder().valid(valid).build())
                .build();
    }

    @PostMapping("/verify")
    public ApiResponse<VerifyOtpResponse> verifyOtp(@RequestBody VerifyOtpRequest request,  HttpServletResponse response) {
        boolean valid = sendMailService.verifyOtp(request.getEmail(), request.getOtp());

        String token =  sendMailService.gentoken(request.getEmail());

        // ✅ Gửi token vào cookie
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Nếu dùng HTTPS thì nên giữ true
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
        response.addCookie(cookie);

        return ApiResponse.<VerifyOtpResponse>builder()
               .result(VerifyOtpResponse.builder()
                       .token(token)
                       .build())
               .build();
    }
}
