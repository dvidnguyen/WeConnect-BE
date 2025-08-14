package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.dto.response.ReadBroadcast;
import com.example.WeConnect_BE.entity.Message;
import com.example.WeConnect_BE.entity.ReadReceipt;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.repository.MemberRepository;
import com.example.WeConnect_BE.repository.MessageRepository;
import com.example.WeConnect_BE.repository.ReadReceiptRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReadReceiptService {
    MessageRepository messageRepository;
    ReadReceiptRepository readReceiptRepository;
    MemberRepository memberRepository;
    UserRepository userRepository;
    @Transactional
    public ReadBroadcast markAsRead(String userId, String messageId) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        String conversationId = msg.getConversation().getId();

        // đảm bảo user là member của conversation
        if (!memberRepository.existsByConversation_IdAndUser_UserId(conversationId, userId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        // idempotent
        if (!readReceiptRepository.existsByMessage_IdAndUser_UserId(messageId, userId)) {
            ReadReceipt rr = new ReadReceipt();
            rr.setMessage(msg);
            rr.setUser(userRepository.getReferenceById(userId)); // không cần load full entity
            rr.setReadAt(LocalDateTime.now());
            readReceiptRepository.save(rr);
        }

        ReadBroadcast out = new ReadBroadcast();
        out.setConversationId(conversationId);
        out.setMessageId(messageId);
        out.setUserId(userId);
        out.setReadAt(LocalDateTime.now());
        return out;
    }
}
