package com.example.iam.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TokenGenerator {
    public static void main(String[] args) {
        String secret = "dGVzdC1zZWNyZXQta2V5LWZvci1sb2NhbC1kZXZlbG9wbWVudC1vbmx5LXBsZWFzZS1jaGFuZ2UtaW4tcHJvZHVjdGlvbg==";
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        String token = Jwts.builder()
                .setSubject("d21e210b-49b5-4d22-8d7d-b5cf5aa7c852") // Driver ID
                .claim("roles", List.of("DRIVER"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("TOKEN: " + token);
    }
}
