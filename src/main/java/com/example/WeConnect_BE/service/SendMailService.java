package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.dto.response.VerifyOtpResponse;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.entity.VerifyCode;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.repository.UserRepository;
import com.example.WeConnect_BE.repository.VerifyCodeRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SendMailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VerifyCodeRepository verifyCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    public void sendOtpEmail(String toEmail, String otp) {





        User user = userRepository.findByEmail(toEmail)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        List<VerifyCode> oldCodes =  verifyCodeRepository.findByUser(user);

        for (VerifyCode oldCode : oldCodes) {
            oldCode.setStatus(1);
            verifyCodeRepository.save(oldCode);
        }

        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setUser(user);
        verifyCode.setCode(otp);
        verifyCode.setCreatedAt(LocalDateTime.now());
        verifyCode.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        verifyCodeRepository.save(verifyCode);



        // Gửi mail HTML
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🔐 Your One-Time Password (OTP)");

            // HTML template có logo
            String content = """
    <html>
      <body style="font-family: Arial, sans-serif; background-color:#f9f9f9; padding:20px;">
        <div style="max-width:600px; margin:auto; background:white; padding:24px; border-radius:10px; box-shadow:0 2px 5px rgba(0,0,0,0.1);">
          
          <!-- Header -->
          <div style="text-align:center; margin-bottom:16px;">
            <div style="font-size:26px; font-weight:bold; color:#2c3e50;">WeConnect</div>
            <div style="font-size:12px; color:#6b7280;">Secure Verification Service</div>
          </div>

          <!-- Title -->
          <h2 style="color:#2c3e50; text-align:center; margin-bottom:20px;">Your Verification Code</h2>

          <!-- Body -->
          <p style="font-size:16px;">Hello <b>%s</b>,</p>
          <p style="font-size:16px; line-height:1.6;">
            You are receiving this email because an OTP (One-Time Password) 
            was requested for your WeConnect account. 
            Please use the code below to complete your verification. 
            This code is valid for <b>5 minutes</b>.
          </p>

          <!-- OTP -->
          <div style="text-align:center; margin:30px 0;">
            <span style="font-size:28px; letter-spacing:6px; font-weight:900; color:#e74c3c; display:inline-block; padding:14px 24px; border:1px dashed #d1d5db; border-radius:8px; background:#f9fafb;">
              %s
            </span>
          </div>

          <!-- Security Notice -->
          <p style="font-size:14px; color:#7f8c8d; line-height:1.5;">
            ⚠️ <b>Important Security Notice:</b><br/>
            - Do NOT share this code with anyone.<br/>
            - WeConnect will never ask you for this code.<br/>
            - If you did not request this OTP, please ignore this email or contact our support team immediately.
          </p>

          <hr style="margin:24px 0; border:none; border-top:1px solid #e5e7eb;"/>

          <!-- Footer -->
          <p style="text-align:center; font-size:12px; color:#95a5a6; margin:0;">
            © 2025 WeConnect. All rights reserved.<br/>
            This is an automated message, please do not reply.
          </p>
        </div>
      </body>
    </html>
    """.formatted(user.getUsername(), otp);



            helper.setText(content, true); // true = cho phép HTML

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    public boolean verifyOtp(String email, String inputOtp) {
        // Tìm user theo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_USER_NOT_FOUND));

        // Lấy mã OTP mới nhất của user còn ACTIVE
        Optional<VerifyCode> optionalOtp = verifyCodeRepository
                .findTopByUserAndStatusOrderByCreatedAtDesc(user, 0);

        if (optionalOtp.isEmpty()) {
            throw new AppException(ErrorCode.OTP_NOT_FOUND_OR_EXPIRED);
        }

        VerifyCode verifyCode = optionalOtp.get();

        // Kiểm tra mã và thời gian hết hạn
        if (!verifyCode.getCode().equals(inputOtp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (verifyCode.getExpiredAt().isBefore(LocalDateTime.now())) {
            // Nếu mã hết hạn
            verifyCode.setStatus(1);
            verifyCodeRepository.save(verifyCode);
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        // Đúng mã, còn hạn → cập nhật trạng thái và chấp nhận
        verifyCode.setStatus(1);
        verifyCodeRepository.save(verifyCode);
        verifyCode.getUser().setStatus(1);
        userRepository.save(user);

        return true;
    }

    public VerifyOtpResponse gentoken(String email) {
       Optional<User> user = userRepository.findByEmail(email);
        String token = authenticationService.generateToken(user.get());

        return VerifyOtpResponse.builder()
                .token(token)
                .avatarUrl(user.get().getAvatarUrl())
                .email(user.get().getEmail())
                .username(user.get().getUsername())
                .build();
    }



}
