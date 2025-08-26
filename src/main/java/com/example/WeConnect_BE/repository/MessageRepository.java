package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    // Trang đầu: block mới nhất (DESC)
    @Query("""
        SELECT m FROM Message m
        WHERE m.conversation.id = :cid
        ORDER BY m.sentAt DESC, m.id DESC
    """)
    List<Message> firstPage(@Param("cid") String conversationId, Pageable pageable);

    @Query("""
    SELECT m FROM Message m
    WHERE m.conversation.id = :cid
      AND (m.sentAt < :sentAt
           OR (m.sentAt = :sentAt AND function('strcmp', m.id, :id) < 0))
    ORDER BY m.sentAt DESC, m.id DESC
  """)
    List<Message> before(@Param("cid") String conversationId,
                         @Param("sentAt") LocalDateTime sentAt,
                         @Param("id") String id,
                         Pageable pageable);

    @Query("""
        SELECT COUNT(m)
        FROM Message m
        WHERE m.conversation.id = :conversationId
          AND m.sender.userId <> :userId
          AND NOT EXISTS (
                SELECT 1
                FROM ReadReceipt rr
                WHERE rr.message = m
                  AND rr.user.userId = :userId
          )
    """)
    long countUnreadForUserInConversation(@Param("conversationId") String conversationId,
                                          @Param("userId") String userId);

    @Query("""
    SELECT m FROM Message m
    WHERE m.conversation.id = :cid
      AND (m.sentAt > :sentAt
           OR (m.sentAt = :sentAt AND function('strcmp', m.id, :id) > 0))
    ORDER BY m.sentAt ASC, m.id ASC
  """)
    List<Message> afterAsc(@Param("cid") String conversationId,
                           @Param("sentAt") LocalDateTime sentAt,
                           @Param("id") String id,
                           Pageable pageable);
}
