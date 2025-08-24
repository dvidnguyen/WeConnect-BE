package com.example.WeConnect_BE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IcePayload {
    private String conversationId;
    private String toUserId;

    // server sẽ set
    private String fromUserId;

    // WebRTC ICE candidate object (dạng map JSON)
    private Map<String, Object> candidate;
}
