package com.example.WeConnect_BE.dto.response;

import com.example.WeConnect_BE.dto.PageInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResult <T>{
    List<T> items;
    PageInfo pageInfo;

}
