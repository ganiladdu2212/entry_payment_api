package com.enty.payment.security;

import com.enty.payment.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Authentication authentication) {
        Instant now = Instant.now();
        return Jwts.builder().subject(authentication.getName()).issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.expiration()))).signWith(key).compact();
    }

    public String getSubject(String token) {
        return parse(token).getSubject();
    }

    public boolean isValid(String token) {
        try { parse(token); return true; } catch (Exception ignored) { return false; }
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
