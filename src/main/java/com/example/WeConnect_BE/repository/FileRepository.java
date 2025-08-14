package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;
@Repository
public interface FileRepository extends JpaRepository<File, String> {
    File findByName(String filename);
}
