package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.Util.GetIDCurent;
import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.response.ListNotification;
import com.example.WeConnect_BE.dto.response.NotificationResponse;
import com.example.WeConnect_BE.service.NotificationUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationUserController {
    NotificationUserService notificationUserService;

    @GetMapping
    public ApiResponse<ListNotification> getNotifications() {
        String userId = GetIDCurent.getId(); // Lấy từ context của bạn
        var data = notificationUserService.getNotifications(userId);
        return ApiResponse.<ListNotification>builder().result(data).build();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> unreadCount() {
        String userId = GetIDCurent.getId();
        var data = notificationUserService.getNotifications(userId).getUnread();
        return ApiResponse.<Integer>builder().result(data).build();
    }



}
