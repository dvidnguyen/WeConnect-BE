package com.example.WeConnect_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class NotificationRaw {
    private String id;
    private String title;
    private String body;
    private String type;
    private String relatedId;
    private Date createdAt;     // từ entity Notification (java.util.Date)
    private boolean isRead;
}
