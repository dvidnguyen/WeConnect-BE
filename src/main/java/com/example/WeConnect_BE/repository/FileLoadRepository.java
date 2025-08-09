package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.dto.FileInfo;
import com.example.WeConnect_BE.entity.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Repository
public class FileLoadRepository {
    @Value("${app.file.storage-dir}")
    String storageDir;
    @Value("${app.file.dowload-prefix}")
    String DowloadPrifex;
    public FileInfo upLoadFile(MultipartFile file) throws IOException {
        Path folder  = Paths.get("D:/upload");
        String urlPrefix = "http://localhost:8080/weconnect/media/";
        String fileExtention = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = Objects.isNull(fileExtention)
                ? UUID.randomUUID().toString()
                : UUID.randomUUID().toString()+"."+ fileExtention;
        Path filePath = folder.resolve(fileName).normalize().toAbsolutePath();
        Files.copy(file.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);
        return FileInfo.builder()
                .name(fileName)
                .type(file.getContentType())
                .md5checksum(DigestUtils.md5DigestAsHex(file.getInputStream()))
                .path(filePath.toString())
                .url(urlPrefix + fileName)
                .build();
    }

    public Resource read(File file) throws IOException {
        var data = Files.readAllBytes(Path.of(file.getPath()));
        return new ByteArrayResource(data);
    }
}
