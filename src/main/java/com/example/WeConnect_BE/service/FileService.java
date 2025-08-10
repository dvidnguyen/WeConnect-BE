package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.dto.response.FileData;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.mapper.FileMapper;
import com.example.WeConnect_BE.repository.FileLoadRepository;
import com.example.WeConnect_BE.repository.FileRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {

    FileLoadRepository fileRepository;
    UserRepository userRepository;
    FileRepository fileMgt;
    FileMapper fileMapper;
    public Object upLoadFile(MultipartFile file) throws IOException {
        var fileInfo = fileRepository.upLoadFile(file);
        var fileMgtOb = fileMapper.toFile(fileInfo);

        return fileMgt.save(fileMgtOb);
    }

    public Object upLoadFileAvatar(MultipartFile file) throws IOException {
        //upload
        var fileInfo = fileRepository.upLoadFile(file);
        // get user
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = authentication.getToken();
        String userId = jwt.getSubject(); // sub trong JWT
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        var fileMgtOb = fileMapper.toFile(fileInfo);

        user.setAvatarUrl(fileMgtOb.getUrl());
        userRepository.save(user);
        return fileMgt.save(fileMgtOb);
    }

    public FileData dowloadFile(String filename) throws IOException {
        var fileMgtob = fileMgt.findById(filename)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
        var resource  = fileRepository.read(fileMgtob);
        return new FileData(fileMgtob.getType(),resource);
    }
}
