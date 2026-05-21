package com.predix.bff.client;

import com.predix.bff.config.PredixProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Sole gateway for fund operations (deposits / withdrawals / balances).
 */
@Component
public class BacpClient extends DownstreamClientSupport {

    public BacpClient(WebClient.Builder builder, PredixProperties properties, MeterRegistry meterRegistry) {
        super(builder, properties.services().bacp().baseUrl(), "bacp", properties, meterRegistry);
    }

    public Map<String, Object> createDeposit(Map<String, Object> request) {
        return post("/api/v1/deposits", request, new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> createWithdrawal(Map<String, Object> request) {
        return post("/api/v1/withdrawals", request, new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> getBalances(String userId) {
        return get("/api/v1/balances?userId=" + userId, new ParameterizedTypeReference<>() {});
    }
}
