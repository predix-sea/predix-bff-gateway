package com.predix.bff.client;

import com.predix.bff.config.PredixProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class MarketSchemaClient extends DownstreamClientSupport {

    public MarketSchemaClient(WebClient.Builder builder, PredixProperties properties, MeterRegistry meterRegistry) {
        super(builder, properties.services().marketSchema().baseUrl(), "market-schema", properties, meterRegistry);
    }

    public List<Map<String, Object>> listMarkets() {
        return get("/api/v1/markets", new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> getMarket(String id) {
        return get("/api/v1/markets/" + id, new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> getOrderbook(String id) {
        return get("/api/v1/markets/" + id + "/orderbook", new ParameterizedTypeReference<>() {});
    }
}
