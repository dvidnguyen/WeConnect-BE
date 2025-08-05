package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {
}
