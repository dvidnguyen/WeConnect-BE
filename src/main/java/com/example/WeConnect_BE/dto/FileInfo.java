package com.example.WeConnect_BE.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileInfo {
    String name;
    String type;
    String md5checksum;
    String path;
    String url;
    String messageId; // thêm field này

}
