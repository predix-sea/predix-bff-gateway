package com.predix.bff.service;

import com.predix.bff.client.BacpClient;
import com.predix.bff.config.PredixProperties;
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

import static org.assertj.core.api.Assertions.assertThat;

class CustodyServiceTest {

    private static MockWebServer server;
    private CustodyService custodyService;
    private RecordingAuditRecorder auditRecorder;

    @BeforeAll
    static void start() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void stop() throws Exception {
        server.shutdown();
    }

    @BeforeEach
    void setUp() {
        auditRecorder = new RecordingAuditRecorder();
        PredixProperties props = new PredixProperties(
                new PredixProperties.JwtProperties("s", "i", Duration.ofHours(1)),
                new PredixProperties.SiweProperties(Duration.ofMinutes(5), "d", "u"),
                new PredixProperties.RateLimitProperties(false, 100),
                new PredixProperties.ComplianceProperties(false, "v1", false, List.of(), ""),
                new PredixProperties.SessionProperties(Duration.ofHours(1)),
                new PredixProperties.ServicesProperties(
                        ep(), ep(),
                        new PredixProperties.ServiceEndpoint(server.url("/").toString()),
                        ep(), ep()),
                new PredixProperties.DownstreamProperties(2000, 5000, 0, 0));
        BacpClient bacpClient = new BacpClient(WebClient.builder(), props, new SimpleMeterRegistry());
        custodyService = new CustodyService(bacpClient, new CustodyPathGuard(), auditRecorder);
    }

    private PredixProperties.ServiceEndpoint ep() {
        return new PredixProperties.ServiceEndpoint("http://localhost:9999");
    }

    @Test
    void depositUsesBacpOnly() {
        server.enqueue(new MockResponse()
                .setBody("{\"id\":\"dep-1\"}")
                .addHeader("Content-Type", "application/json"));
        SessionUser user = new SessionUser("0xabc", 1L, "APPROVED");
        Map<String, Object> result = custodyService.deposit(Map.of("amount", 10), user, "1.2.3.4");
        assertThat(result).containsEntry("id", "dep-1");
        assertThat(auditRecorder.records()).anyMatch(r -> "DEPOSIT".equals(r.action()));
    }
}
