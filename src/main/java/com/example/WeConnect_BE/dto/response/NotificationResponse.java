package com.example.WeConnect_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private String id;
    private String title;
    private String body;
    private String type;
    private String relatedId;
    private LocalDateTime createdAt;

    // Thông tin trạng thái đọc của người dùng
    private boolean isRead;

    // Optional: thông tin user gửi (nếu cần)
    private String senderId;
    private String senderName;
    private String senderAvatarUrl;

}
