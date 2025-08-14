package com.example.WeConnect_BE.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReadBroadcast {
    String conversationId;
    String messageId;
    String userId;
    LocalDateTime readAt;
}
