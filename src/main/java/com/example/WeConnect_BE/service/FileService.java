package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.dto.response.FileData;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.mapper.FileMapper;
import com.example.WeConnect_BE.repository.FileLoadRepository;
import com.example.WeConnect_BE.repository.FileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {

    FileLoadRepository fileRepository;
    FileRepository fileMgt;
    FileMapper fileMapper;
    public Object upLoadFile(MultipartFile file) throws IOException {
        var fileInfo = fileRepository.upLoadFile(file);
        var fileMgtOb = fileMapper.toFile(fileInfo);

        return fileMgt.save(fileMgtOb);
    }
    public FileData dowloadFile(String filename) throws IOException {
        var fileMgtob = fileMgt.findById(filename)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
        var resource  = fileRepository.read(fileMgtob);
        return new FileData(fileMgtob.getType(),resource);
    }
}
