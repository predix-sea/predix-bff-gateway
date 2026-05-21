package com.predix.bff.compliance;

import com.predix.bff.config.PredixProperties;
import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import com.predix.bff.security.SessionUser;
import com.predix.bff.support.RecordingAuditRecorder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ComplianceServiceTest {

    private ComplianceService complianceService;
    private RecordingAuditRecorder auditRecorder;

    @BeforeEach
    void setUp() {
        auditRecorder = new RecordingAuditRecorder();
        GeoIpResolver geoIpResolver = new GeoIpResolver(complianceProps()) {
            @Override
            public Optional<String> resolveCountry(String ip) {
                if ("203.0.113.1".equals(ip)) {
                    return Optional.of("CN");
                }
                return Optional.of("US");
            }
        };
        complianceService = new ComplianceService(complianceProps(), geoIpResolver, auditRecorder, new SimpleMeterRegistry());
    }

    private PredixProperties complianceProps() {
        return new PredixProperties(
                new PredixProperties.JwtProperties("s", "i", Duration.ofHours(1)),
                new PredixProperties.SiweProperties(Duration.ofMinutes(5), "d", "u"),
                new PredixProperties.RateLimitProperties(false, 100),
                new PredixProperties.ComplianceProperties(true, "v1", true,
                        List.of("SG", "TH", "MY", "PH", "VN", "ID"), ""),
                new PredixProperties.SessionProperties(Duration.ofHours(1)),
                new PredixProperties.ServicesProperties(ep(), ep(), ep(), ep(), ep()),
                new PredixProperties.DownstreamProperties(1, 1, 0, 0));
    }

    private PredixProperties.ServiceEndpoint ep() {
        return new PredixProperties.ServiceEndpoint("http://localhost");
    }

    @Test
    void blocksMainlandChina() {
        ClientContext ctx = complianceService.buildContext("203.0.113.1", null);
        assertThatThrownBy(() -> complianceService.enforceGeoPolicy(ctx, "/api/v1/markets"))
                .isInstanceOf(BffException.class)
                .extracting(e -> ((BffException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMPLIANCE_CN_BLOCKED);
        assertThat(auditRecorder.records()).isNotEmpty();
    }

    @Test
    void kycRequiredForTrading() {
        SessionUser pending = new SessionUser("0xabc", 1L, "PENDING");
        ClientContext ctx = new ClientContext("8.8.8.8", "US", null, "v1");
        assertThatThrownBy(() -> complianceService.enforceAccess(ctx, "/api/v1/orders", pending))
                .isInstanceOf(BffException.class)
                .extracting(e -> ((BffException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMPLIANCE_KYC_REQUIRED);
    }

    @Test
    void allowsReadWithoutKyc() {
        SessionUser pending = new SessionUser("0xabc", 1L, "PENDING");
        ClientContext ctx = new ClientContext("8.8.8.8", "US", null, "v1");
        complianceService.enforceAccess(ctx, "/api/v1/markets", pending);
    }
}
