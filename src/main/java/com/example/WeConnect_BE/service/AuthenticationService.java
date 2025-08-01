package com.example.WeConnect_BE.service;

import com.example.WeConnect_BE.dto.request.AuthenticationRequest;
import com.example.WeConnect_BE.dto.request.IntrospectRequest;
import com.example.WeConnect_BE.dto.request.RegisterRequest;
import com.example.WeConnect_BE.dto.response.AuthenticationResponse;
import com.example.WeConnect_BE.dto.response.IntrospectResponse;
import com.example.WeConnect_BE.dto.response.RegisterReponse;
import com.example.WeConnect_BE.entity.User;
import com.example.WeConnect_BE.exception.AppException;
import com.example.WeConnect_BE.exception.ErrorCode;
import com.example.WeConnect_BE.repository.InvalidTokenRepository;
import com.example.WeConnect_BE.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidTokenRepository invalidTokenRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    private String KEY_SIGNTURE;
    @NonFinal
    @Value("${jwt.valid-duration}")
    private long validDuration;
    @NonFinal
    @Value("${jwt.refresh-duration}")
    private long refreshDuration;

    public IntrospectResponse introspect(IntrospectRequest request) throws AppException, JOSEException, ParseException {
        String token = request.getToken();

        try {
            verifyToken(token, false);
        } catch (Exception e) {
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }

        return IntrospectResponse.builder()
                .valid(true)
                .build();


    }
    public List<User> getUser() throws AppException, JOSEException, ParseException {
        return userRepository.findAll();
    }
//    public RegisterReponse register(RegisterRequest request){
//        //check email exist
//        boolean valid = userRepository.existsByEmail(request.getEmail());
//        // store otp to db
//
//        //send opt to client mail
//        return null;
//    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean authenticated =  passwordEncoder.matches(authenticationRequest.getPassword(), user.getPasswordHash());
        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(authenticated)
                .build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
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


    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
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


}



