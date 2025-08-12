package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.response.ContactResponse;
import com.example.WeConnect_BE.service.ContactService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContactController {
    ContactService contactService;

    @GetMapping("/")
    public ApiResponse<List<ContactResponse>> getAllContacts() {
        return ApiResponse.<List<ContactResponse>>builder()
                .result(contactService.getContacts())
                .build();
    }

    @PostMapping("/block/{user_id}")
    public ApiResponse<String> blockContact(@PathVariable("user_id") String id) {
        return ApiResponse.<String>builder()
                .code(200)
                .result(contactService.blockContact(id))
                .build();
    }

    @DeleteMapping("/unblock/{blockedUserId}")
    public ApiResponse<String> unblock(@PathVariable("blockedUserId") String blockedUserId) {
        return ApiResponse.<String>builder()
                .code(200)
                .result(contactService.unblock(blockedUserId))
                .build();
    }

    @DeleteMapping("/cancel/{userId}")
    public ApiResponse<String> cancelContact(@PathVariable("userId") String id) {
        contactService.unfriend(id);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Successfully cancelled contact")
                .build();
    }

}
