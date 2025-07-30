package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

}
