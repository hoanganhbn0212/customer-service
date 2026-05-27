package com.customerservice.service;

import com.customerservice.config.AuthJwtProperties;
import com.customerservice.entity.AppUser;
import com.customerservice.exception.UnauthorizedException;
import com.customerservice.security.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_UID = "uid";

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtTokenService(AuthJwtProperties properties) {
        byte[] secretBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationSeconds = properties.getExpirationSeconds();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String createAccessToken(AppUser user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_ROLE, user.getRole())
                .claim(CLAIM_UID, user.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public AuthUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String role = claims.get(CLAIM_ROLE, String.class);
            String uid = claims.get(CLAIM_UID, String.class);
            if (role == null || uid == null) {
                throw new UnauthorizedException();
            }
            return new AuthUser(uid, claims.getSubject(), role);
        } catch (Exception ex) {
            throw new UnauthorizedException();
        }
    }
}
