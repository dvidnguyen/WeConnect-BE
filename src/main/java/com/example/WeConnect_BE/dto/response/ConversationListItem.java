package com.example.WeConnect_BE.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationListItem {
    String conversationId;
    String name;
    String type;
    String avatar;
    String lastMessage;
    LocalDateTime lastMessageTime;
    long unreadCount;
}
