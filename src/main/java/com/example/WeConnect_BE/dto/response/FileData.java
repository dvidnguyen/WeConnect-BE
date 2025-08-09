package com.example.WeConnect_BE.dto.response;

import org.springframework.core.io.Resource;

public record FileData(String contentType, Resource data) {
}
