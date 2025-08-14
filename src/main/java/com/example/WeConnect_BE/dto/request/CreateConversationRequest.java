package com.example.WeConnect_BE.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateConversationRequest {
    String type;          // "direct" | "group"
    String targetUserId;  // bắt buộc nếu DIRECT
    String name;          // bắt buộc nếu GROUP
    List<String> memberIds; // danh sách thành viên nếu GROUP (có thể rỗng, mình sẽ auto thêm creator)
}
