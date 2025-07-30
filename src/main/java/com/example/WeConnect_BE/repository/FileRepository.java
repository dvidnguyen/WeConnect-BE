package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {}
