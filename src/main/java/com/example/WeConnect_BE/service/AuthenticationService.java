package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.dto.request.*;
import com.example.WeConnect_BE.dto.response.AuthenticationResponse;
import com.example.WeConnect_BE.dto.response.IntrospectResponse;
import com.example.WeConnect_BE.dto.response.RegisterReponse;
import com.example.WeConnect_BE.entity.InvalidToken;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.mapper.UserMapper;
import com.example.WeConnect_BE.repository.InvalidTokenRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import com.example.WeConnect_BE.repository.UserSessionRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    UserSessionRepository userSessionRepository;
    InvalidTokenRepository invalidTokenRepository;
    UserMapper userMapper;
    @NonFinal
    @Value("${jwt.signerKey}")
    private String KEY_SIGNTURE;
    @NonFinal
    @Value("${jwt.valid-duration}")
    private long validDuration;
    @NonFinal
    @Value("${jwt.refresh-duration}")
    private long refreshDuration;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public IntrospectResponse introspect(IntrospectRequest request) throws AppException, JOSEException, ParseException {
        String token = request.getToken();
        SignedJWT signJwt = null;
        try {
             signJwt = verifyToken(token, false);
        } catch (Exception e) {
            return IntrospectResponse.builder()
                    .UserId(Objects.nonNull(signJwt) ? signJwt.getJWTClaimsSet().getSubject() : null)
                    .valid(false)
                    .build();
        }

        return IntrospectResponse.builder()
                .UserId(Objects.nonNull(signJwt) ? signJwt.getJWTClaimsSet().getSubject() : null)
                .valid(true)
                .build();

    }
    public List<User> getUser() throws AppException, JOSEException, ParseException {
        return userRepository.findAll();
    }
    public RegisterReponse register(RegisterRequest request){
        // 1. Check email tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 2. Map request -> entity
        User user = userMapper.toUser(request);

        // 3. Hash password
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPasswordHash(encodedPassword);
        // thời gian tạo và update
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 4. Set status mặc định
        user.setStatus(0);

        // 5. Lưu user
        userRepository.save(user);

        // 6. Trả về response
        return RegisterReponse.builder()
                .email(user.getEmail())
                .valid(true)
                .build();
    }


    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserId())
                .issuer("duc.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(KEY_SIGNTURE.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

    }


    private SignedJWT   verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(KEY_SIGNTURE.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date exp = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getExpirationTime().toInstant().plus(refreshDuration, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean valid = signedJWT.verify(verifier);
        if (!(valid && exp.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (invalidTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }


    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean authenticated =  passwordEncoder.matches(authenticationRequest.getPassword(), user.getPasswordHash());
        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = generateToken(user);
        return AuthenticationResponse.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .authenticated(authenticated)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), false);

        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        InvalidToken invalidatedToken = InvalidToken.builder()
                .token(jti)
                .createdAt(new Date())
                .build();
        invalidTokenRepository.save(invalidatedToken);
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest req) throws ParseException, JOSEException {
        var signedJWT = verifyToken(req.getToken(), true);
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        InvalidToken invalidatedToken = InvalidToken.builder()
                .token(jti)
                .createdAt(new Date())
                .build();
        invalidTokenRepository.save(invalidatedToken);

        var user = userRepository.findById(signedJWT.getJWTClaimsSet().getSubject()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTS)
        );
        String token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }




}



