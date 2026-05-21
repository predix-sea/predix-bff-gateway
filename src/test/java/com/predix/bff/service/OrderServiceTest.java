package com.predix.bff.service;

import com.predix.bff.client.MatchingEngineClient;
import com.predix.bff.compliance.ComplianceService;
import com.predix.bff.compliance.GeoIpResolver;
import com.predix.bff.config.PredixProperties;
import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import com.predix.bff.security.SessionUser;
import com.predix.bff.support.RecordingAuditRecorder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderServiceTest {

    private static MockWebServer matchingServer;
    private OrderService orderService;

    @BeforeAll
    static void start() throws Exception {
        matchingServer = new MockWebServer();
        matchingServer.start();
    }

    @AfterAll
    static void stop() throws Exception {
        matchingServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        PredixProperties props = new PredixProperties(
                new PredixProperties.JwtProperties("s", "i", Duration.ofHours(1)),
                new PredixProperties.SiweProperties(Duration.ofMinutes(5), "d", "u"),
                new PredixProperties.RateLimitProperties(false, 100),
                new PredixProperties.ComplianceProperties(false, "v1", false, List.of(), ""),
                new PredixProperties.SessionProperties(Duration.ofHours(1)),
                new PredixProperties.ServicesProperties(
                        ep(), new PredixProperties.ServiceEndpoint(matchingServer.url("/").toString()),
                        ep(), ep(), ep()),
                new PredixProperties.DownstreamProperties(2000, 5000, 0, 0));
        MatchingEngineClient matching = new MatchingEngineClient(WebClient.builder(), props, new SimpleMeterRegistry());
        GeoIpResolver geoIpResolver = new GeoIpResolver(props) {
            @Override
            public Optional<String> resolveCountry(String ip) {
                return Optional.empty();
            }
        };
        ComplianceService compliance = new ComplianceService(props, geoIpResolver,
                new RecordingAuditRecorder(), new SimpleMeterRegistry());
        orderService = new OrderService(matching, compliance);
    }

    private PredixProperties.ServiceEndpoint ep() {
        return new PredixProperties.ServiceEndpoint("http://localhost:9999");
    }

    @Test
    void placeOrder_requiresKyc() {
        assertThatThrownBy(() -> orderService.placeOrder(Map.of("side", "BUY"),
                        new SessionUser("0xabc", 1L, "PENDING")))
                .isInstanceOf(BffException.class)
                .extracting(e -> ((BffException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMPLIANCE_KYC_REQUIRED);
    }

    @Test
    void placeOrder_success() {
        matchingServer.enqueue(new MockResponse().setBody("{\"orderId\":\"o1\"}").addHeader("Content-Type", "application/json"));
        Map<String, Object> result = orderService.placeOrder(Map.of("side", "BUY"),
                new SessionUser("0xabc", 1L, "APPROVED"));
        assertThat(result).containsEntry("orderId", "o1");
    }
}
