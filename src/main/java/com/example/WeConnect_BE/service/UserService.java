package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.Util.PhoneUtil;
import com.example.WeConnect_BE.dto.request.EditUserRequest;
import com.example.WeConnect_BE.dto.response.SearchUserResponse;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.mapper.UserMapper;
import com.example.WeConnect_BE.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    public List<SearchUserResponse> search(String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }

        // Nếu là email
        if (PhoneUtil.looksLikeEmail(q)) {
            return userRepository.findByEmailIgnoreCase(q.trim())
                    .map(userMapper::toSearchUserResponse)     // map sang DTO
                    .map(List::of)                              // list 1 phần tử
                    .orElseGet(List::of);                       // rỗng
        }

        // Nếu là số điện thoại
        if (PhoneUtil.looksLikePhone(q)) {

            return userRepository.findByPhoneNormalized(q)
                    .map(userMapper::toSearchUserResponse)
                    .map(List::of)
                    .orElseGet(List::of);
        }

        // Fallback: tìm gần đúng
        return userRepository.searchLoose(q.trim(), q)
                .stream()
                .map(userMapper::toSearchUserResponse)
                .toList();
    }


    public void editUser( EditUserRequest req) {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = authentication.getToken();
        String userId = jwt.getSubject(); // sub trong JWT
        // Kiểm tra trùng số điện thoại
        if (req.getPhone() != null && userRepository.existsByPhoneAndNotUserId(req.getPhone(), userId)) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS); // bạn tự định nghĩa ErrorCode
        }
        // Lấy user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Cập nhật field
        user.setUsername(req.getUsername());
        user.setBirthDate(req.getBirthDate());
        user.setPhone(req.getPhone());
        user.setUpdatedAt(LocalDateTime.now());


        // Lưu
        userRepository.save(user);
    }
}
