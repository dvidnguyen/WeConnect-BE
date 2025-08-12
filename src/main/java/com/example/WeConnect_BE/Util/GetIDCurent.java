package com.example.WeConnect_BE.Util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class GetIDCurent {
    public static String getId() {

        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        Jwt jwt = authentication.getToken();
        String userId = jwt.getSubject();
        return userId;
    }
}
