package com.example.WeConnect_BE.mapper;

import com.example.WeConnect_BE.dto.FileInfo;
import com.example.WeConnect_BE.entity.File;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMapper {
    // Entity → DTO
    @Mapping(source = "message.id", target = "messageId") // lấy messageId từ message
    FileInfo toFileInfo(File file);

    // DTO → Entity
    File toFile(FileInfo fileInfo);
}
