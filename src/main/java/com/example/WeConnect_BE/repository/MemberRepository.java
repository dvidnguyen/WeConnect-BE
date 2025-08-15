package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    boolean existsByConversation_IdAndUser_UserId(String conversationId, String userId);

    @Query("select m.user.userId from Member m where m.conversation.id = :cid")
    List<String> findUserIdsByConversation(@Param("cid") String conversationId);

    @Query("select m.conversation.id from Member m where m.user.userId = :uid")
    List<String> findConversationIdsByUser(@Param("uid") String userId);

    Optional<Member> findByConversation_IdAndUser_UserId(String conversationId, String userId);

    long countByConversation_IdAndRole(String conversationId, Member.Role role);

    void deleteByConversation_IdAndUser_UserId(String conversationId, String userId);


    // Lấy 1 ứng viên admin (người vào sớm nhất, khác user rời)
    @Query("select m from Member m where m.conversation.id = :cid and m.user.userId <> :exclude order by m.joinedAt asc")
    List<Member> findAdminCandidate(@Param("cid") String conversationId,
                                    @Param("exclude") String exclude,
                                    Pageable pageable);

//    boolean existsByConversationIdAndUserId(String conversationId, String senderId);

    // MemberRepository (thêm method lọc trừ người gửi)
    @Query("select m.user.userId from Member m " +
            "where m.conversation.id = :conversationId and m.user.userId <> :excludeUserId")
    List<String> findUserIdsByConversationExcept(@Param("conversationId") String conversationId,
                                                 @Param("excludeUserId") String excludeUserId);
}
