package com.predix.bff.service;

import com.predix.bff.audit.AuditEventType;
import com.predix.bff.audit.AuditRecorder;
import com.predix.bff.client.BacpClient;
import com.predix.bff.security.SessionUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustodyService {

    private final BacpClient bacpClient;
    private final CustodyPathGuard custodyPathGuard;
    private final AuditRecorder auditRecorder;

    public CustodyService(BacpClient bacpClient, CustodyPathGuard custodyPathGuard, AuditRecorder auditRecorder) {
        this.bacpClient = bacpClient;
        this.custodyPathGuard = custodyPathGuard;
        this.auditRecorder = auditRecorder;
    }

    public Map<String, Object> deposit(Map<String, Object> request, SessionUser user, String clientIp) {
        return custodyPathGuard.executeInBacpContext(() -> {
            CustodyPathGuard.assertBacpOnly("deposit");
            auditRecorder.log(AuditEventType.CUSTODY_ACTION, user.walletAddress(), clientIp, null,
                    "/api/v1/custody/deposits", "DEPOSIT", "REQUEST", request.toString());
            return bacpClient.createDeposit(request);
        });
    }

    public Map<String, Object> withdraw(Map<String, Object> request, SessionUser user, String clientIp) {
        return custodyPathGuard.executeInBacpContext(() -> {
            CustodyPathGuard.assertBacpOnly("withdrawal");
            auditRecorder.log(AuditEventType.CUSTODY_ACTION, user.walletAddress(), clientIp, null,
                    "/api/v1/custody/withdrawals", "WITHDRAW", "REQUEST", request.toString());
            return bacpClient.createWithdrawal(request);
        });
    }

    public Map<String, Object> balances(String userId, SessionUser user, String clientIp) {
        return custodyPathGuard.executeInBacpContext(() -> {
            CustodyPathGuard.assertBacpOnly("balance");
            auditRecorder.log(AuditEventType.CUSTODY_ACTION, user.walletAddress(), clientIp, null,
                    "/api/v1/custody/balances", "BALANCE", "REQUEST", "userId=" + userId);
            return bacpClient.getBalances(userId);
        });
    }
}
