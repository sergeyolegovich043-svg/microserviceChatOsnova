package com.example.securechat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
        this.verificationKey = Keys.hmacShaKeyFor(publicKey.getBytes());
        this.expectedIssuer = expectedIssuer;
        this.expectedAudience = expectedAudience;
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
