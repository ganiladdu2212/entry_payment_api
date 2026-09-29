package com.enty.payment.security;

import com.enty.payment.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String subject) {
        return createToken(subject, properties.accessExpiration(), "ACCESS");
    }

    public String createRefreshToken(String subject) {
        return createToken(subject, properties.refreshExpiration(), "REFRESH");
    }

    public long getAccessExpirationSeconds() {
        return properties.accessExpiration().toSeconds();
    }

    private String createToken(String subject, java.time.Duration expiration, String tokenType) {
        Instant now = Instant.now();
        return Jwts.builder().subject(subject).claim("tokenType", tokenType).issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration))).signWith(key).compact();
    }

    public String getSubject(String token) {
        return parse(token).getSubject();
    }

    public boolean isValidAccessToken(String token) {
        try { return "ACCESS".equals(parse(token).get("tokenType", String.class)); }
        catch (Exception ignored) { return false; }
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
