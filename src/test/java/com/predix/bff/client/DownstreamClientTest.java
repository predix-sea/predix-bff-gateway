package com.predix.bff.client;

import com.predix.bff.config.PredixProperties;
import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DownstreamClientTest {

    private static MockWebServer server;
    private MarketSchemaClient client;

    @BeforeAll
    static void beforeAll() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void afterAll() throws Exception {
        server.shutdown();
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
                        new PredixProperties.ServiceEndpoint(server.url("/").toString()),
                        new PredixProperties.ServiceEndpoint("http://localhost:8082"),
                        new PredixProperties.ServiceEndpoint("http://localhost:8083"),
                        new PredixProperties.ServiceEndpoint("http://localhost:8084"),
                        new PredixProperties.ServiceEndpoint("http://localhost:8085")),
                new PredixProperties.DownstreamProperties(1000, 2000, 0, 0));
        client = new MarketSchemaClient(WebClient.builder(), props, new SimpleMeterRegistry());
    }

    @Test
    void getMarkets_success() {
        server.enqueue(new MockResponse()
                .setBody("[{\"id\":\"m1\"}]")
                .addHeader("Content-Type", "application/json"));
        List<Map<String, Object>> markets = client.listMarkets();
        assertThat(markets).hasSize(1);
    }

    @Test
    void getMarkets_unavailable() {
        server.enqueue(new MockResponse().setResponseCode(503));
        assertThatThrownBy(() -> client.listMarkets())
                .isInstanceOf(BffException.class)
                .extracting(e -> ((BffException) e).getErrorCode())
                .isEqualTo(ErrorCode.DOWNSTREAM_UNAVAILABLE);
    }
}
