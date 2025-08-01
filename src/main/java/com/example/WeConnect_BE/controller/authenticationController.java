package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.request.RegisterRequest;
import com.example.WeConnect_BE.dto.response.RegisterReponse;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.service.AuthenticationService;
import com.example.WeConnect_BE.service.SendMailService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class authenticationController {
    AuthenticationService authenticationService;


    @PostMapping("/user")
    public ApiResponse<List<User>> getuser() throws ParseException, JOSEException {
        return ApiResponse.<List<User>>builder()
                .result(authenticationService.getUser())
                .build();
    }


    @PostMapping("/regis")
    public ApiResponse<RegisterReponse> regis(@RequestBody RegisterRequest request) throws ParseException, JOSEException {
        return ApiResponse.<RegisterReponse>builder()
                .result(authenticationService.register(request))
                .build();
    }
}
