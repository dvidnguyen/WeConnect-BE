package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.ReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ReadReceiptRepository extends JpaRepository<ReadReceipt, UUID> {}
