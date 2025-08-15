package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.Util.SocketEmitter;
import com.example.WeConnect_BE.dto.response.MessageResponse;
import com.example.WeConnect_BE.entity.*;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <— dùng transaction của Spring
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {

    FileService fileService;
    MessageRepository messageRepository;
    UserRepository userRepository;
    ConversationRepository conversationRepository;
    MemberRepository memberRepository;
    ConversationService conversationService;
    SocketEmitter socketEmitter;
    ReadReceiptRepository readReceiptRepository; // nếu có

    @Transactional
    public MessageResponse createMessage(
            String senderId,
            String conversationId,
            String content,
            Message.Type type,
            List<MultipartFile> files
    ) {
        // 1) Load & validate
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        // Kiểm tra membership
        boolean isMember = memberRepository.existsByConversation_IdAndUser_UserId(conversationId, senderId);
        if (!isMember) {
            throw new AppException(ErrorCode.FORBIDDEN); // hoặc lỗi riêng: NOT_CONVERSATION_MEMBER
        }

        // 2) Xác định type hợp lệ
        boolean hasFiles = files != null && !files.isEmpty();
        if (type == null) {
            type = hasFiles ? Message.Type.file : Message.Type.text;
        }
        if (!hasFiles && (content == null || content.isBlank())) {
            throw new AppException(ErrorCode.BAD_REQUEST); // phải có content hoặc file
        }

        // 3) Tạo message (chưa persist file)
        Message message = Message.builder()
                .content(content != null && !content.isBlank() ? content : null)
                .sender(sender)
                .conversation(conversation)
                .type(type)
                .sentAt(LocalDateTime.now())
                .build();

        // 4) Xử lý upload file (nếu có)
        List<com.example.WeConnect_BE.entity.File> fileList = new ArrayList<>();
        if (hasFiles) {
            for (MultipartFile mf : files) {
                try {
                    // upLoadFileMessage nên: lưu vật lý, tạo File entity gắn message, và return entity đã persist (hoặc chưa – tùy bạn)
                    com.example.WeConnect_BE.entity.File savedFile = fileService.upLoadFileMessage(mf, message);
                    fileList.add(savedFile);
                } catch (IOException ex) {
                    // ném AppException để rollback toàn bộ
                    throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
                }
            }
            message.setFiles(fileList);
        }

        // 5) Lưu message (nếu fileService không tự save message)
        // Nếu upLoadFileMessage đã set message đúng quan hệ và cascade ALL thì save message 1 lần là đủ
        Message saved = messageRepository.save(message);

        // 6) ReadReceipt cho người gửi (khuyến nghị)
        tryCreateSenderReadReceipt(saved, sender);

        // 7) Emit real-time
        Set<String> recipients = new LinkedHashSet<>(memberRepository.findUserIdsByConversation(conversationId));
        recipients.remove(senderId);
        MessageResponse payload = conversationService.toDTO(saved);
        socketEmitter.emitToUsers(recipients, "message", payload);

        return payload;
    }

    private void tryCreateSenderReadReceipt(Message message, User sender) {
        if (readReceiptRepository == null) return; // trường hợp bạn chưa khai báo repo
        ReadReceipt rr = new ReadReceipt();
        rr.setMessage(message);
        rr.setUser(sender);
        rr.setReadAt(LocalDateTime.now());
        readReceiptRepository.save(rr);
    }
}
