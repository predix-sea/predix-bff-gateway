package com.predix.bff.service;

import com.predix.bff.client.MatchingEngineClient;
import com.predix.bff.compliance.ComplianceService;
import com.predix.bff.security.SessionUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderService {

    private final MatchingEngineClient matchingEngineClient;
    private final ComplianceService complianceService;

    public OrderService(MatchingEngineClient matchingEngineClient, ComplianceService complianceService) {
        this.matchingEngineClient = matchingEngineClient;
        this.complianceService = complianceService;
    }

    public Map<String, Object> placeOrder(Map<String, Object> order, SessionUser user) {
        if (user == null || !user.isKycApproved()) {
            throw new com.predix.bff.exception.BffException(com.predix.bff.exception.ErrorCode.COMPLIANCE_KYC_REQUIRED);
        }
        return matchingEngineClient.placeOrder(order);
    }

    public Map<String, Object> cancelOrder(String orderId, Map<String, Object> body, SessionUser user) {
        if (user == null || !user.isKycApproved()) {
            throw new com.predix.bff.exception.BffException(com.predix.bff.exception.ErrorCode.COMPLIANCE_KYC_REQUIRED);
        }
        return matchingEngineClient.cancelOrder(orderId, body);
    }
}
