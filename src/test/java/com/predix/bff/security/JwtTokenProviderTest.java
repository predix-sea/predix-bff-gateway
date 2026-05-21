package com.predix.bff.security;

import com.predix.bff.config.PredixProperties;
import com.predix.bff.exception.BffException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider(new PredixProperties(
            new PredixProperties.JwtProperties("test-secret-key-at-least-32-characters", "predix-bff", Duration.ofHours(1)),
            new PredixProperties.SiweProperties(Duration.ofMinutes(5), "d", "u"),
            new PredixProperties.RateLimitProperties(false, 100),
            new PredixProperties.ComplianceProperties(false, "v1", false, java.util.List.of(), ""),
            new PredixProperties.SessionProperties(Duration.ofHours(1)),
            new PredixProperties.ServicesProperties(ep(), ep(), ep(), ep(), ep()),
            new PredixProperties.DownstreamProperties(1, 1, 0, 0)));

    private PredixProperties.ServiceEndpoint ep() {
        return new PredixProperties.ServiceEndpoint("http://localhost");
    }

    @Test
    void createAndParseToken() {
        String sessionId = provider.newSessionId();
        String token = provider.createToken("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd", 1L, sessionId);
        var claims = provider.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd");
        assertThat(claims.getId()).isEqualTo(sessionId);
    }

    @Test
    void invalidTokenThrows() {
        assertThatThrownBy(() -> provider.parseToken("invalid.token.here"))
                .isInstanceOf(BffException.class);
    }
}
