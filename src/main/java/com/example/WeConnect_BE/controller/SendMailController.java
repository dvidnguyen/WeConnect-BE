package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.Util.OtpUtil;
import com.example.WeConnect_BE.service.SendMailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SendMailController {
    SendMailService sendMailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        String otp = OtpUtil.generateOtp(6); // 6 chữ số

        // Gửi mail
        sendMailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok("OTP sent to email: " + email);
    }
}
