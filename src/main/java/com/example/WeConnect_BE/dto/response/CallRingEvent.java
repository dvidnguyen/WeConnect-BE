package com.example.WeConnect_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallRingEvent {
    private String conversationId;
    private String fromUserId;
    private String media; // "video" | "audio"
}
