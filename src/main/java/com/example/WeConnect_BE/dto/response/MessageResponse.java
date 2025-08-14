package com.example.WeConnect_BE.dto.response;

import com.example.WeConnect_BE.entity.File;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageResponse {
    String id;
    String conversationId;
    String senderId;
    String senderName;
    String SenderAvatar;
    String type;
    long receipt;
    long reaction;
    List<String> url;
    List<String> urlDownload;
    String content;
    Instant sentAt;
    boolean mine;
}
