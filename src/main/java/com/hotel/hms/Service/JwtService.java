package com.hotel.hms.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String secret =
            "hotelManagementSystemSecretKeyForJwtAuthentication123456789";

    private final SecretKey secretKey =
            Keys.hmacShaKeyFor(
                    secret.getBytes(StandardCharsets.UTF_8)
            );

    private final long expirationTime =
            1000 * 60 * 60; // 1 hour

    // Generate JWT token
    public String generateToken(String email, String role) {

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expirationTime
                        )
                )
                .signWith(
                        secretKey,
                        Jwts.SIG.HS384
                )
                .compact();
    }

    // Extract email
    public String extractEmail(String token) {

        return getClaims(token)
                .getSubject();
    }

    // Extract role
    public String extractRole(String token) {

        return getClaims(token)
                .get("role", String.class);
    }

    // Validate token
public boolean isTokenValid(String token) {
    try {
        getClaims(token);
        return true;

    } catch (Exception exception) {

        System.out.println(
                "JWT VALIDATION ERROR: "
                        + exception.getClass().getSimpleName()
                        + " - "
                        + exception.getMessage()
        );

        return false;
    }
}

    // Read JWT claims
    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}