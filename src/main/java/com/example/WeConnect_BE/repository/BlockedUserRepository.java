package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, UUID> {}
