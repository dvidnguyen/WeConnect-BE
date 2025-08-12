package com.example.WeConnect_BE.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {

    //Mở/tạo chat 1–1 (find-or-create)
    //Tạo group
    //Lấy danh sách conversation (kèm lastMessage + unreadCount)
//    Lấy lịch sử tin nhắn (paging)
    //Thêm/Xóa/Thoát nhóm, đổi tên/ảnh nhóm
}
