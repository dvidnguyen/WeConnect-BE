package com.example.WeConnect_BE.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EditUserRequest {
    String username;
    Date birthDate;
    String phone;
}
