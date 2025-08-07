package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.ReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ReadReceiptRepository extends JpaRepository<ReadReceipt, UUID> {}
