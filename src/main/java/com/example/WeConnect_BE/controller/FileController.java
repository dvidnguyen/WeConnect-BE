package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

    FileService fileService;

    @PostMapping("/media/upload")
    ApiResponse<Object> uploadMedia(@RequestParam("file") MultipartFile file) throws IOException {
//        return ApiResponse.<Object>builder()
//                .result(fileService.upLoadFile(file))
//                .build();
        return null;
    }
    //    CONTENT_DISPOSITION
//            CACHE_CONTROL
    @GetMapping("/media/{filename}")
    ResponseEntity<Resource> dowload(@PathVariable String filename) throws IOException {
//        var filedata = fileService.dowloadFile(filename);
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, filedata.contentType()).body(filedata.data());
        return null;
    }
}
