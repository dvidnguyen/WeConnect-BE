package com.example.WeConnect_BE.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

public interface ConversationRow {
    String getConversationId();
    String getName();
    String getType();            // ConversationType được lưu dạng TEXT/ENUM -> map ra String
    String getAvatar();

    String getLastMessageId();
    String getLastMessage();
    LocalDateTime getLastMessageTime();
    String getLastMessageSenderId();

    Long getUnreadCount();
}
