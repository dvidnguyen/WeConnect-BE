package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, String> {

    // 1 chiều: userId chặn blockedUserId?
    boolean existsByUser_UserIdAndBlockedUser_UserId(String userId, String blockedUserId);

    // 2 chiều: u1 chặn u2 hoặc u2 chặn u1?
    @Query("""
           select (count(b) > 0) 
           from BlockedUser b
           where (b.user.userId = :u1 and b.blockedUser.userId = :u2)
              or (b.user.userId = :u2 and b.blockedUser.userId = :u1)
           """)
    boolean existsEitherDirection(@Param("u1") String u1, @Param("u2") String u2);

    // phục vụ unblock hoặc lấy record cụ thể
    Optional<BlockedUser> findByUser_UserIdAndBlockedUser_UserId(String userId, String blockedUserId);
    long deleteByUser_UserIdAndBlockedUser_UserId(String userId, String blockedUserId);
}
