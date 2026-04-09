package com.company.product.api.security;

import com.company.product.api.config.AppProperties;
import com.company.product.api.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final AppProperties appProperties;
    private final SecretKey key;

    public JwtTokenService(AppProperties appProperties) {
        this.appProperties = appProperties;
        String secret = appProperties.jwt().secret();
        byte[] bytes = resolveSecret(secret);
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String generate(UserAccount user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("role", user.getRole().name())
            .claim("fullName", user.getFullName())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(appProperties.jwt().expirationMinutes(), ChronoUnit.MINUTES)))
            .signWith(key)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private byte[] resolveSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret не задан");
        }
        byte[] utf8 = secret.getBytes(StandardCharsets.UTF_8);
        if (utf8.length >= 32) {
            return utf8;
        }
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length < 32) {
                throw new IllegalArgumentException("JWT secret слишком короткий");
            }
            return decoded;
        } catch (DecodingException exception) {
            throw new IllegalArgumentException("JWT secret должен быть не короче 32 байт", exception);
        }
    }
}
