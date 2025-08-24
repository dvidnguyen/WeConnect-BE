package com.example.WeConnect_BE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallInvitePayload {
    private String conversationId;
    // "video" | "audio"
    private String media;
}
