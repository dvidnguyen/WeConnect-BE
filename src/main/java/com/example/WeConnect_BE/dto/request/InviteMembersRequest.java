package com.example.WeConnect_BE.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InviteMembersRequest {
    List<String> userIds;
    String defaultRole = "member";
}
