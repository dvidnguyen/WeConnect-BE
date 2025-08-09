package com.example.WeConnect_BE.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchUserResponse {
    String userId;
    String email;
    String username;
    Date birthDate;
    String phone;
    String avatarUrl;



}
