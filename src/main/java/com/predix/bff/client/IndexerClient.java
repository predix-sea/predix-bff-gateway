package com.predix.bff.client;

import com.predix.bff.config.PredixProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class IndexerClient extends DownstreamClientSupport {

    public IndexerClient(WebClient.Builder builder, PredixProperties properties, MeterRegistry meterRegistry) {
        super(builder, properties.services().indexer().baseUrl(), "indexer", properties, meterRegistry);
    }

    public List<Map<String, Object>> getPositions(String marketId, String userId) {
        return get("/api/v1/markets/" + marketId + "/positions?userId=" + userId,
                new ParameterizedTypeReference<>() {});
    }
}
