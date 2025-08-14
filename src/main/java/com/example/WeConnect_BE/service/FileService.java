package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.dto.response.FileData;
import com.example.WeConnect_BE.entity.Conversation;
import com.example.WeConnect_BE.entity.File;
import com.example.WeConnect_BE.entity.Message;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.mapper.FileMapper;
import com.example.WeConnect_BE.repository.ConversationRepository;
import com.example.WeConnect_BE.repository.FileLoadRepository;
import com.example.WeConnect_BE.repository.FileRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
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
    ConversationRepository conversationRepository;
    FileRepository fileMgt;
    FileMapper fileMapper;
    public File upLoadFileMessage(MultipartFile file, Message m) throws IOException {
        var fileInfo = fileRepository.upLoadFile(file);
        File fileMgtOb = fileMapper.toFile(fileInfo);

        fileMgtOb.setMessage(m);

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
        File fileMgtob = null;
        Resource resource = null;
        try {
            fileMgtob = fileMgt.findByName((filename));
            resource = fileRepository.read(fileMgtob);
           
        } catch (Exception e) {
            new AppException(ErrorCode.FILE_NOT_FOUND);
        }
        return new FileData(fileMgtob.getType(), resource);
    }

    public Object uploadFileAvatarConversation(MultipartFile file, String convId) throws IOException {
        //upload
        var fileInfo = fileRepository.upLoadFile(file);
        // get user

        Conversation conversation = conversationRepository.findById(convId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        var fileMgtOb = fileMapper.toFile(fileInfo);

        conversation.setAvatar(fileMgtOb.getUrl());
        conversationRepository.save(conversation);
        return fileMgt.save(fileMgtOb);
    }
}
