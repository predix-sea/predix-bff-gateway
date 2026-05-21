package com.predix.bff.service;

import com.predix.bff.client.*;
import com.predix.bff.config.PredixProperties;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SystemHealthService {

    private final PredixProperties properties;
    private final MarketSchemaClient marketSchemaClient;
    private final MatchingEngineClient matchingEngineClient;
    private final BacpClient bacpClient;
    private final IndexerClient indexerClient;
    private final OracleOpsClient oracleOpsClient;

    public SystemHealthService(PredixProperties properties,
                               MarketSchemaClient marketSchemaClient,
                               MatchingEngineClient matchingEngineClient,
                               BacpClient bacpClient,
                               IndexerClient indexerClient,
                               OracleOpsClient oracleOpsClient) {
        this.properties = properties;
        this.marketSchemaClient = marketSchemaClient;
        this.matchingEngineClient = matchingEngineClient;
        this.bacpClient = bacpClient;
        this.indexerClient = indexerClient;
        this.oracleOpsClient = oracleOpsClient;
    }

    public Map<String, Object> dependenciesHealth() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("marketSchema", probe("market-schema", properties.services().marketSchema().baseUrl()));
        result.put("matching", probe("matching-engine", properties.services().matching().baseUrl()));
        result.put("bacp", probe("bacp", properties.services().bacp().baseUrl()));
        result.put("indexer", probe("indexer", properties.services().indexer().baseUrl()));
        result.put("oracleOps", probe("oracle-ops", properties.services().oracleOps().baseUrl()));
        return result;
    }

    private Map<String, Object> probe(String name, String baseUrl) {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("name", name);
        status.put("baseUrl", baseUrl);
        try {
            // lightweight probe – configured URL reachable check deferred to integration
            status.put("status", "CONFIGURED");
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
        }
        return status;
    }
}
