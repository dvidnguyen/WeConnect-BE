package com.example.WeConnect_BE.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FriendPendingResponse {
    String id;
    String requesterId;
    String requesterName;
    String requesterAvatar;
    LocalDateTime sentAt;
}
