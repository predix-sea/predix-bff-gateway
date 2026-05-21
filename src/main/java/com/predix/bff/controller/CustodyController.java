package com.predix.bff.controller;

import com.predix.bff.compliance.IpExtractor;
import com.predix.bff.security.AuthenticatedUserHolder;
import com.predix.bff.service.CustodyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/custody")
public class CustodyController {

    private final CustodyService custodyService;

    public CustodyController(CustodyService custodyService) {
        this.custodyService = custodyService;
    }

    @PostMapping("/deposits")
    public Map<String, Object> deposit(@RequestBody @NotEmpty Map<String, Object> request, HttpServletRequest http) {
        return custodyService.deposit(request, AuthenticatedUserHolder.get(), IpExtractor.extractClientIp(http));
    }

    @PostMapping("/withdrawals")
    public Map<String, Object> withdraw(@RequestBody @NotEmpty Map<String, Object> request, HttpServletRequest http) {
        return custodyService.withdraw(request, AuthenticatedUserHolder.get(), IpExtractor.extractClientIp(http));
    }

    @GetMapping("/balances")
    public Map<String, Object> balances(@RequestParam String userId, HttpServletRequest http) {
        return custodyService.balances(userId, AuthenticatedUserHolder.get(), IpExtractor.extractClientIp(http));
    }
}
