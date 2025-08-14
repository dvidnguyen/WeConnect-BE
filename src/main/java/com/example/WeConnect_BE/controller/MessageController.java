package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.Util.GetIDCurent;
import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.response.MessageResponse;
import com.example.WeConnect_BE.entity.Message;
import com.example.WeConnect_BE.repository.MessageRepository;
import com.example.WeConnect_BE.service.MessageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageController {
     MessageService messageService;
//    tạo message

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MessageResponse> createMessage(
            @RequestParam String conversationId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Message.Type type,
            @RequestPart(required = false) List<MultipartFile> files
    ) {
        // Lấy ID người dùng hiện tại (từ token hoặc SecurityContext)
        String senderId = GetIDCurent.getId();


        return ApiResponse.<MessageResponse>builder()
                .code(200)
                .message("success")
                .result(messageService.createMessage(senderId, conversationId, content, type, files))
                .build();
    }
}
