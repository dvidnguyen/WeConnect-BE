package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.entity.VerifyCode;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.repository.UserRepository;
import com.example.WeConnect_BE.repository.VerifyCodeRepository;
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

    @Autowired AuthenticationService authenticationService;

    public void sendOtpEmail(String toEmail, String otp) {



        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + "\nIt will expire in 5 minutes.");

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

        mailSender.send(message);
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

    public String gentoken(String email) {
       Optional<User> user = userRepository.findByEmail(email);
        String token = authenticationService.generateToken(user.get());
        return token;
    }



}
