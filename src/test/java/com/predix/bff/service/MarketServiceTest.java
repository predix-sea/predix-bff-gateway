package com.predix.bff.service;

import com.predix.bff.client.IndexerClient;
import com.predix.bff.client.MarketSchemaClient;
import com.predix.bff.config.PredixProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MarketServiceTest {

    private static MockWebServer schemaServer;
    private static MockWebServer indexerServer;
    private MarketService marketService;

    @BeforeAll
    static void start() throws Exception {
        schemaServer = new MockWebServer();
        indexerServer = new MockWebServer();
        schemaServer.start();
        indexerServer.start();
    }

    @AfterAll
    static void stop() throws Exception {
        schemaServer.shutdown();
        indexerServer.shutdown();
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
                        new PredixProperties.ServiceEndpoint(schemaServer.url("/").toString()),
                        new PredixProperties.ServiceEndpoint("http://localhost:8082"),
                        new PredixProperties.ServiceEndpoint("http://localhost:8083"),
                        new PredixProperties.ServiceEndpoint(indexerServer.url("/").toString()),
                        new PredixProperties.ServiceEndpoint("http://localhost:8085")),
                new PredixProperties.DownstreamProperties(2000, 5000, 0, 0));
        MarketSchemaClient schemaClient = new MarketSchemaClient(WebClient.builder(), props, new SimpleMeterRegistry());
        IndexerClient indexerClient = new IndexerClient(WebClient.builder(), props, new SimpleMeterRegistry());
        marketService = new MarketService(schemaClient, indexerClient);
    }

    @Test
    void listMarkets() {
        schemaServer.enqueue(new MockResponse().setBody("""
                {"code":"0","message":"Success","data":{"content":[{"id":"m1"}],"page":0,"size":20,"totalElements":1,"totalPages":1}}
                """).addHeader("Content-Type", "application/json"));
        assertThat(marketService.listMarkets()).hasSize(1);
    }

    @Test
    void getMarket_enriched() {
        schemaServer.enqueue(new MockResponse().setBody("""
                {"code":"0","message":"Success","data":{"id":"m1","title":"Test"}}
                """).addHeader("Content-Type", "application/json"));
        Map<String, Object> market = marketService.getMarket("m1");
        assertThat(market).containsEntry("source", "market-schema");
    }

    @Test
    void getOrderbook() {
        schemaServer.enqueue(new MockResponse().setBody("""
                {"code":"0","message":"Success","data":{"bids":[]}}
                """).addHeader("Content-Type", "application/json"));
        assertThat(marketService.getOrderbook("m1")).containsKey("bids");
    }

    @Test
    void getPositions() {
        indexerServer.enqueue(new MockResponse().setBody("[{\"userId\":\"u1\"}]").addHeader("Content-Type", "application/json"));
        assertThat(marketService.getPositions("m1", "u1")).hasSize(1);
    }
}
