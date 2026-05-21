package com.predix.bff.controller;

import com.predix.bff.security.AuthenticatedUserHolder;
import com.predix.bff.service.OrderService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Map<String, Object> placeOrder(@RequestBody @NotEmpty Map<String, Object> order) {
        return orderService.placeOrder(order, AuthenticatedUserHolder.get());
    }

    @PostMapping("/{id}/cancel")
    public Map<String, Object> cancelOrder(@PathVariable String id, @RequestBody Map<String, Object> body) {
        return orderService.cancelOrder(id, body != null ? body : Map.of(), AuthenticatedUserHolder.get());
    }
}
