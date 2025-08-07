package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface FriendRepository extends JpaRepository<Friend, String> {
    boolean existsByRequesterUserIdAndAddresseeUserId(String from, String to);

    Optional<Friend> findByRequesterUserIdAndAddresseeUserId(String requesterId, String addresseeId);

    List<Friend> findByAddressee_IdAndStatus(String address, Friend.FriendStatus status);
}
