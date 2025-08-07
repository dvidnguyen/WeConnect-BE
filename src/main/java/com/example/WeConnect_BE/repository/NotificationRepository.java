package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {}