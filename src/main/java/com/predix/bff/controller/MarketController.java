package com.predix.bff.controller;

import com.predix.bff.service.MarketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/markets")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping
    public List<Map<String, Object>> listMarkets() {
        return marketService.listMarkets();
    }

    @GetMapping("/{id}")
    public Map<String, Object> getMarket(@PathVariable String id) {
        return marketService.getMarket(id);
    }

    @GetMapping("/{id}/orderbook")
    public Map<String, Object> getOrderbook(@PathVariable String id) {
        return marketService.getOrderbook(id);
    }

    @GetMapping("/{id}/positions")
    public List<Map<String, Object>> getPositions(@PathVariable String id, @RequestParam String userId) {
        return marketService.getPositions(id, userId);
    }
}
