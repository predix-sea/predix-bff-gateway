package com.predix.bff.client;

import com.predix.bff.config.PredixProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class MatchingEngineClient extends DownstreamClientSupport {

    public MatchingEngineClient(WebClient.Builder builder, PredixProperties properties, MeterRegistry meterRegistry) {
        super(builder, properties.services().matching().baseUrl(), "matching-engine", properties, meterRegistry);
    }

    public Map<String, Object> placeOrder(Map<String, Object> order) {
        return post("/api/v1/orders", order, new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> cancelOrder(String orderId, Map<String, Object> body) {
        return post("/api/v1/orders/" + orderId + "/cancel", body, new ParameterizedTypeReference<>() {});
    }
}
