package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {}
