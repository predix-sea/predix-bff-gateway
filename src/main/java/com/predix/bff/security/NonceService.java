package com.predix.bff.security;

import com.predix.bff.config.PredixProperties;
import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Service
public class NonceService {

    private static final String NONCE_PREFIX = "siwe:nonce:";
    private final StringRedisTemplate redis;
    private final Duration ttl;
    private final SecureRandom secureRandom = new SecureRandom();

    public NonceService(StringRedisTemplate redis, PredixProperties properties) {
        this.redis = redis;
        this.ttl = properties.siwe().nonceTtl();
    }

    public String generateNonce() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redis.opsForValue().set(NONCE_PREFIX + nonce, "1", ttl);
        return nonce;
    }

    public void consumeNonce(String nonce) {
        String key = NONCE_PREFIX + nonce;
        Boolean deleted = redis.delete(key);
        if (deleted == null || !deleted) {
            throw new BffException(ErrorCode.AUTH_NONCE_EXPIRED);
        }
    }

    public boolean exists(String nonce) {
        return Boolean.TRUE.equals(redis.hasKey(NONCE_PREFIX + nonce));
    }

    public Optional<Duration> getTtl(String nonce) {
        Long seconds = redis.getExpire(NONCE_PREFIX + nonce);
        if (seconds == null || seconds < 0) {
            return Optional.empty();
        }
        return Optional.of(Duration.ofSeconds(seconds));
    }
}
