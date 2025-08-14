package com.example.WeConnect_BE.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InviteMembersResponse {
    String conversationId;
    List<String> added;  // userIds đã thêm
    List<String> alreadyMembers;  // userIds đã ở trong group
    List<String> notFound;  // userIds không tồn tại
}
