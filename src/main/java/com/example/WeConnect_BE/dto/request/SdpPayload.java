package com.example.WeConnect_BE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SdpPayload {
    private String conversationId;
    private String toUserId;

    // server sẽ set
    private String fromUserId;

    // SDP string
    private String sdp;
}
