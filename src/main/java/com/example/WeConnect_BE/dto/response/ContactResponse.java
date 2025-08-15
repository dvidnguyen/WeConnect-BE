package com.example.WeConnect_BE.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContactResponse {
    String id;
    String name;
    String email;
    String avatarUrl;
    boolean isBlock;
}
