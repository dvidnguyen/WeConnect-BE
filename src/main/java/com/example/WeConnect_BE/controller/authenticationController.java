package com.example.WeConnect_BE.controller;

import com.example.WeConnect_BE.dto.ApiResponse;
import com.example.WeConnect_BE.dto.request.RegisterRequest;
import com.example.WeConnect_BE.dto.response.RegisterReponse;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.service.AuthenticationService;
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

//    @GetMapping("/introspect")
//    public String introspect() {
//        return "hello";
//    }
    @PostMapping("/user")
    public ApiResponse<List<User>> getuser() throws ParseException, JOSEException {
        return ApiResponse.<List<User>>builder()
                .result(authenticationService.getUser())
                .build();
    }

//    @PostMapping("/log-in")
//    public ApiResponse<AuthenticationResponse> logIn(@RequestBody AuthenticationRequest authenticationRequest) {
//        AuthenticationResponse result = authenticationService.authenticate(authenticationRequest);
//        return ApiResponse.<AuthenticationResponse>builder()
//                .result(result)
//                .build();

//    @PostMapping("/user")
//    public ApiResponse<RegisterReponse> regis(@RequestParam RegisterRequest request) throws ParseException, JOSEException {
//        return ApiResponse.<List<User>>builder()
//                .result(authenticationService.getUser())
//                .build();
//    }
}
