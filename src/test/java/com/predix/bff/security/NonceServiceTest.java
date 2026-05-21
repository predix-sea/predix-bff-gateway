package com.predix.bff.security;

import com.predix.bff.config.PredixProperties;
import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import com.predix.bff.support.TestRedisSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NonceServiceTest {

    private NonceService nonceService;
    private StringRedisTemplate redis;

    @BeforeEach
    void setUp() {
        redis = TestRedisSupport.createTemplate();
        PredixProperties props = new PredixProperties(
                new PredixProperties.JwtProperties("secret", "issuer", Duration.ofHours(1)),
                new PredixProperties.SiweProperties(Duration.ofMinutes(5), "predix.local", "https://predix.local"),
                new PredixProperties.RateLimitProperties(false, 100),
                new PredixProperties.ComplianceProperties(true, "v1", true, java.util.List.of("SG"), ""),
                new PredixProperties.SessionProperties(Duration.ofHours(24)),
                new PredixProperties.ServicesProperties(
                        new PredixProperties.ServiceEndpoint("http://localhost:8081"),
                        new PredixProperties.ServiceEndpoint("http://localhost:8082"),
                        new PredixProperties.ServiceEndpoint("http://localhost:8083"),
                        new PredixProperties.ServiceEndpoint("http://localhost:8084"),
                        new PredixProperties.ServiceEndpoint("http://localhost:8085")),
                new PredixProperties.DownstreamProperties(3000, 10000, 2, 200));
        nonceService = new NonceService(redis, props);
    }

    @Test
    void generateNonce_storesInRedisWithTtl() {
        String nonce = nonceService.generateNonce();
        assertThat(nonce).isNotBlank();
        assertThat(nonceService.exists(nonce)).isTrue();
    }

    @Test
    void consumeNonce_deletesKey() {
        String nonce = nonceService.generateNonce();
        nonceService.consumeNonce(nonce);
        assertThat(nonceService.exists(nonce)).isFalse();
    }

    @Test
    void consumeNonce_missingThrows() {
        assertThatThrownBy(() -> nonceService.consumeNonce("missing"))
                .isInstanceOf(BffException.class)
                .extracting(e -> ((BffException) e).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_NONCE_EXPIRED);
    }
}
