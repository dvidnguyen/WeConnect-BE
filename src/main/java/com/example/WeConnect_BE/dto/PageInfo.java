package com.example.WeConnect_BE.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageInfo {
    boolean hasPrevPage;  // còn bản ghi mới hơn startCursor?
    boolean hasNextPage; // còn bản ghi cũ hơn endCursor?
    String startCursor; // cursor phần tử đầu (mới nhất)
    String endCursor; // cursor phần tử cuối (cũ nhất)
}
