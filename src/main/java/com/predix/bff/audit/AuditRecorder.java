package com.predix.bff.audit;

public interface AuditRecorder {

    void log(AuditEventType type, String wallet, String ip, String country,
             String resource, String action, String outcome, String detail);
}
