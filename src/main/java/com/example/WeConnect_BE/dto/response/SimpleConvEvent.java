package com.example.WeConnect_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimpleConvEvent {
    private String conversationId;
    private String userId; // người thực hiện hành động (accept/reject/end)

}
