package com.predix.bff.service;

import com.predix.bff.client.IndexerClient;
import com.predix.bff.client.MarketSchemaClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarketService {

    private final MarketSchemaClient marketSchemaClient;
    private final IndexerClient indexerClient;

    public MarketService(MarketSchemaClient marketSchemaClient, IndexerClient indexerClient) {
        this.marketSchemaClient = marketSchemaClient;
        this.indexerClient = indexerClient;
    }

    public List<Map<String, Object>> listMarkets() {
        return marketSchemaClient.listMarkets();
    }

    public Map<String, Object> getMarket(String id) {
        Map<String, Object> market = marketSchemaClient.getMarket(id);
        Map<String, Object> enriched = new HashMap<>(market);
        enriched.put("source", "market-schema");
        return enriched;
    }

    public Map<String, Object> getOrderbook(String id) {
        return marketSchemaClient.getOrderbook(id);
    }

    public List<Map<String, Object>> getPositions(String marketId, String userId) {
        return indexerClient.getPositions(marketId, userId);
    }
}
