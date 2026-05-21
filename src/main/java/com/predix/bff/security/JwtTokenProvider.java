package com.predix.bff.security;

import com.predix.bff.config.PredixProperties;
import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final String issuer;
    private final java.time.Duration ttl;

    public JwtTokenProvider(PredixProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.issuer = properties.jwt().issuer();
        this.ttl = properties.jwt().accessTokenTtl();
    }

    public String createToken(String walletAddress, long chainId, String sessionId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(sessionId)
                .subject(walletAddress.toLowerCase())
                .issuer(issuer)
                .claim("chainId", chainId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new BffException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    public String newSessionId() {
        return UUID.randomUUID().toString();
    }
}
