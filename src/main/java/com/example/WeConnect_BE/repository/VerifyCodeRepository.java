package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.entity.VerifyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface VerifyCodeRepository extends JpaRepository<VerifyCode, UUID> {
    Optional<VerifyCode> findTopByUserOrderByCreatedAtDesc(User user);

    List<VerifyCode> findByUser(User user);

    Optional<VerifyCode> findTopByUserAndStatusOrderByCreatedAtDesc(User user, int i);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update VerifyCode v set v.status = 1 where v.user = :user and v.status = 0")
    int deactivateActiveCodesByUser(User user);
}
