package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FriendRepository extends JpaRepository<Friend, UUID> {}
