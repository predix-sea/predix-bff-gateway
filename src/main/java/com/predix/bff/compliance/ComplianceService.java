package com.predix.bff.compliance;

import com.predix.bff.audit.AuditEventType;
import com.predix.bff.audit.AuditRecorder;
import com.predix.bff.config.PredixProperties;
import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import com.predix.bff.security.SessionUser;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ComplianceService {

    private static final Set<String> TRADING_PATH_PREFIXES = Set.of(
            "/api/v1/orders",
            "/api/v1/custody"
    );

    private final PredixProperties.ComplianceProperties config;
    private final GeoIpResolver geoIpResolver;
    private final AuditRecorder auditRecorder;
    private final MeterRegistry meterRegistry;

    public ComplianceService(PredixProperties properties,
                             GeoIpResolver geoIpResolver,
                             AuditRecorder auditRecorder,
                             MeterRegistry meterRegistry) {
        this.config = properties.compliance();
        this.geoIpResolver = geoIpResolver;
        this.auditRecorder = auditRecorder;
        this.meterRegistry = meterRegistry;
    }

    public ClientContext buildContext(String clientIp, String countryHeader) {
        String country = geoIpResolver.resolveCountry(clientIp).orElse(countryHeader);
        return new ClientContext(clientIp, country, countryHeader, config.policyVersion());
    }

    public void enforceAccess(ClientContext ctx, String path, SessionUser user) {
        if (!config.enabled()) {
            return;
        }
        enforceGeoPolicy(ctx, path);
        if (requiresKyc(path) && (user == null || !user.isKycApproved())) {
            incrementBlock("kyc");
            auditRecorder.log(AuditEventType.COMPLIANCE_BLOCK, user != null ? user.walletAddress() : null,
                    ctx.clientIp(), ctx.countryCode(), path, "KYC_GATE", "BLOCKED", "KYC required");
            throw new BffException(ErrorCode.COMPLIANCE_KYC_REQUIRED);
        }
    }

    public void enforceGeoPolicy(ClientContext ctx, String path) {
        if (!config.enabled()) {
            return;
        }
        String country = resolveEffectiveCountry(ctx);
        if (config.blockMainlandChina() && "CN".equalsIgnoreCase(country)) {
            incrementBlock("cn");
            auditRecorder.log(AuditEventType.COMPLIANCE_BLOCK, null, ctx.clientIp(), "CN",
                    path, "CN_BLOCK", "BLOCKED", "Mainland China blocked");
            throw new BffException(ErrorCode.COMPLIANCE_CN_BLOCKED);
        }
    }

    public boolean requiresKyc(String path) {
        return TRADING_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    public List<String> countryPriority() {
        return config.countryPriority();
    }

    private String resolveEffectiveCountry(ClientContext ctx) {
        if (ctx.countryCode() != null && !ctx.countryCode().isBlank()) {
            return ctx.countryCode();
        }
        if (ctx.countryFromHeader() != null && !ctx.countryFromHeader().isBlank()) {
            return ctx.countryFromHeader();
        }
        return "";
    }

    private void incrementBlock(String reason) {
        Counter.builder("bff_compliance_block_total")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }
}
