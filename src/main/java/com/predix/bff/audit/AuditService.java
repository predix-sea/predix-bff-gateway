package com.predix.bff.audit;

import com.predix.bff.config.TraceIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService implements AuditRecorder {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Async
    public void log(AuditEventType type, String wallet, String ip, String country,
                      String resource, String action, String outcome, String detail) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setTraceId(TraceIdFilter.currentTraceId());
        entity.setEventType(type.name());
        entity.setActorWallet(wallet);
        entity.setClientIp(ip);
        entity.setCountryCode(country);
        entity.setResource(resource);
        entity.setAction(action);
        entity.setOutcome(outcome);
        entity.setDetailJson(detail);
        try {
            repository.save(entity);
        } catch (Exception e) {
            log.warn("Failed to persist audit log, falling back to structured log: {}", e.getMessage());
        }
        log.info("audit event={} wallet={} ip={} country={} resource={} action={} outcome={} detail={}",
                type, wallet, ip, country, resource, action, outcome, detail);
    }
}
