package com.predix.bff.client;

import com.predix.bff.config.PredixProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class OracleOpsClient extends DownstreamClientSupport {

    public OracleOpsClient(WebClient.Builder builder, PredixProperties properties, MeterRegistry meterRegistry) {
        super(builder, properties.services().oracleOps().baseUrl(), "oracle-ops", properties, meterRegistry);
    }

    public Map<String, Object> health() {
        return get("/actuator/health", new ParameterizedTypeReference<>() {});
    }
}
