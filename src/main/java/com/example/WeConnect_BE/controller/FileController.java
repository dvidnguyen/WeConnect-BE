package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.Util.RenameFile;
import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {

    FileService fileService;

//    @PostMapping("/media/upload")
//    ApiResponse<Object> uploadMedia(@RequestParam("file") MultipartFile file) throws IOException {
//      return ApiResponse.<Object>builder()
//              .result(fileService.upLoadFile(file))
//               .build();
//        return null;
//    }

    @PostMapping("/avatar/upload")
    ApiResponse<Object> uploadMediaAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.<Object>builder()
                .result(fileService.upLoadFileAvatar(file))
                .build();
    }

    @PostMapping("/avatar/upload/group/{convId}")
    ApiResponse<Object> uploadMediaAvatarGroup(@RequestParam("file") MultipartFile file, @PathVariable("convId") String id) throws IOException {
        return ApiResponse.<Object>builder()
                .result(fileService.uploadFileAvatarConversation(file,id))
                .build();
    }

    //    CONTENT_DISPOSITION
//            CACHE_CONTROL
    @GetMapping("/media/{filename}")
    ResponseEntity<Resource> download(@PathVariable String filename) throws IOException {
        var filedata = fileService.dowloadFile(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, filedata.contentType()).body(filedata.data());
    }

    @GetMapping("/media/{filename}/download")
    ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        var filedata = fileService.dowloadFile(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + RenameFile.rename(filename, "download") + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(filedata.data());
    }
}
