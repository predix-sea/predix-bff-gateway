package com.predix.bff.service;

import com.predix.bff.config.PredixProperties;
import com.predix.bff.dto.auth.SiweVerifyRequest;
import com.predix.bff.exception.BffException;
import com.predix.bff.security.*;
import com.predix.bff.support.RecordingAuditRecorder;
import com.predix.bff.support.TestRedisSupport;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private AuthService authService;
    private NonceService nonceService;
    private StringRedisTemplate redis;

    @BeforeEach
    void setUp() {
        redis = TestRedisSupport.createTemplate();
        PredixProperties props = props();
        nonceService = new NonceService(redis, props);
        authService = new AuthService(
                nonceService,
                new SiweVerifier(),
                new JwtTokenProvider(props),
                new SessionService(redis, props, new com.fasterxml.jackson.databind.ObjectMapper()),
                new RecordingAuditRecorder(),
                new SimpleMeterRegistry().counter("bff_auth_fail_total"),
                props);
    }

    @Test
    void createNonce_returnsMessageWithNonce() {
        var response = authService.createNonce();
        assertThat(response.nonce()).isNotBlank();
        assertThat(response.message()).contains(response.nonce());
    }

    @Test
    void verify_invalidSignatureFails() {
        String nonce = authService.createNonce().nonce();
        String message = "Login for 0x0000000000000000000000000000000000000001\nNonce: " + nonce;
        SiweVerifyRequest req = new SiweVerifyRequest(
                "0x0000000000000000000000000000000000000002",
                message,
                "0x" + "11".repeat(65),
                1L);
        assertThatThrownBy(() -> authService.verify(req, "127.0.0.1"))
                .isInstanceOf(BffException.class);
    }

    private PredixProperties props() {
        return new PredixProperties(
                new PredixProperties.JwtProperties("test-secret-key-at-least-32-characters", "predix-bff", Duration.ofHours(1)),
                new PredixProperties.SiweProperties(Duration.ofMinutes(5), "predix.local", "https://predix.local"),
                new PredixProperties.RateLimitProperties(false, 100),
                new PredixProperties.ComplianceProperties(false, "v1", false, java.util.List.of(), ""),
                new PredixProperties.SessionProperties(Duration.ofHours(1)),
                new PredixProperties.ServicesProperties(
                        ep(), ep(), ep(), ep(), ep()),
                new PredixProperties.DownstreamProperties(1, 1, 0, 0));
    }

    private PredixProperties.ServiceEndpoint ep() {
        return new PredixProperties.ServiceEndpoint("http://localhost");
    }
}
