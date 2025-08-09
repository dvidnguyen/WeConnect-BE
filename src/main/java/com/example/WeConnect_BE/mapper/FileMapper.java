package com.example.WeConnect_BE.mapper;

import com.example.WeConnect_BE.dto.FileInfo;
import com.example.WeConnect_BE.entity.File;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {
    File toFile(FileInfo fileInfo);
}
