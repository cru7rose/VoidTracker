package com.example.iam.service;

import com.example.iam.entity.UserEntity;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ARCHITEKTURA: Dedykowany serwis w iam-service, odpowiedzialny wyłącznie za
 * generowanie tokenów JWT. Jest to jedyne miejsce w całym systemie, które
 * ma logikę tworzenia tokenów.
 */
@Service
public class TokenService {

    private final Key secretKey;
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 godzin

    public TokenService(@Value("${jwt.secret.key}") String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserEntity user,
            java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
        return generateToken(user, authorities, EXPIRATION_TIME);
    }

    public String generateToken(UserEntity user,
                                java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities,
                                long expirationTimeMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId().toString());
        claims.put("roles", authorities.stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return createToken(claims, user.getUsername(), expirationTimeMillis);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return createToken(claims, subject, EXPIRATION_TIME);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationTimeMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillis))
                .signWith(secretKey)
                .compact();
    }
}