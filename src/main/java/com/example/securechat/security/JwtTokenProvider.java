package com.example.securechat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final Key verificationKey;
    private final String expectedIssuer;
    private final String expectedAudience;

    public JwtTokenProvider(
            @Value("${security.jwt.public-key}") String publicKey,
            @Value("${security.jwt.issuer:}") String expectedIssuer,
            @Value("${security.jwt.audience:}") String expectedAudience) {
        this.verificationKey = createVerificationKey(publicKey);
        this.expectedIssuer = expectedIssuer;
        this.expectedAudience = expectedAudience;
    }

    private Key createVerificationKey(String publicKey) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(publicKey);
        } catch (IllegalArgumentException e) {
            keyBytes = publicKey.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "security.jwt.public-key must be at least 256 bits (32 bytes) after decoding");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Optional<String> validateAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(verificationKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            if (!expectedIssuer.isEmpty() && !expectedIssuer.equals(claims.getIssuer())) {
                return Optional.empty();
            }
            if (!expectedAudience.isEmpty() && !expectedAudience.equals(claims.getAudience())) {
                return Optional.empty();
            }
            Object userId = claims.get("userId", Object.class);
            if (userId == null) {
                userId = claims.getSubject();
            }
            return userId == null ? Optional.empty() : Optional.of(userId.toString());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
