package com.company.product.api.security;

import com.company.product.api.config.AppProperties;
import com.company.product.api.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
        byte[] bytes = secret.length() >= 32 ? secret.getBytes(StandardCharsets.UTF_8) : Decoders.BASE64.decode(secret);
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
}
