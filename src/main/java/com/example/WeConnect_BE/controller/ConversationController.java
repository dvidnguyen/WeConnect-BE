package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.Util.GetIDCurent;
import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.request.CreateConversationRequest;
import com.example.WeConnect_BE.dto.request.InviteMembersRequest;
import com.example.WeConnect_BE.dto.response.*;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.service.ConversationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conversations")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {
    ConversationService conversationService;

    //Mở/tạo chat 1–1 (find-or-create)
    //Tạo group

    @PostMapping("/create")
    public ApiResponse<ConversationResponse> create(@RequestBody CreateConversationRequest req) {
        ConversationResponse res = conversationService.createConversation(req);
        return ApiResponse.<ConversationResponse>builder()
                .result(res)
                .message(res.isCreated() ? "Created" : "Exists")
                .build();
    }
    //Lấy danh sách conversation (kèm lastMessage + unreadCount)

    @GetMapping("/{userId}")
    public ApiResponse<List<ConversationRow>> list(@PathVariable("userId") String userId) {
        var data = conversationService.getList();
        return ApiResponse.<List<ConversationRow>>builder()
                .result(data)
                .build();
    }
    //    Lấy lịch sử tin nhắn (paging)*
    @GetMapping("/{conversationId}/messages")
    public ApiResponse<PageResult<MessageResponse>> list(
            @PathVariable String conversationId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String before,
            @RequestParam(required = false) String after
    ) {
//        if (before != null && after != null)
//            throw new AppException(ErrorCode.BAD_REQUEST);
//        // (khuyến nghị) xác thực user là member của conversationId tại đây

        return ApiResponse.<PageResult<MessageResponse>>builder()
                .code(200)
                .message("successfull")
                .result(conversationService.getMessagesPerConversation(conversationId, limit, before, after))
                .build();
    }


    // mời vào nhóm

    @PostMapping("/{conversationId}/members/invite")
    public ApiResponse<InviteMembersResponse> invite(
            @PathVariable String conversationId,
            @RequestBody InviteMembersRequest req
    ) {
        String inviterUserId = GetIDCurent.getId(); // lấy id user hiện tại theo dự án của bạn
        var res = conversationService.inviteMembers(conversationId, inviterUserId, req);
        return ApiResponse.<InviteMembersResponse>builder()
                .code(200)
                .message("successfull")
                .result(res)
                .build();
    }

    //rời nhóm

    @PostMapping("/{conversationId}/leave")
    public ApiResponse<LeaveGroupResponse> leave(@PathVariable String conversationId) {
        String currentUserId = GetIDCurent.getId();
        var res = conversationService.leaveGroup(conversationId, currentUserId);
        return ApiResponse.<LeaveGroupResponse>builder()
                .code(200)
                .message("successfull")
                .result(res)
                .build();
    }

    //Thêm/Xóa/Thoát nhóm, đổi tên/ảnh nhóm
}
