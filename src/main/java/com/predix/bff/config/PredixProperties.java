package com.predix.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "predix")
public record PredixProperties(
        JwtProperties jwt,
        SiweProperties siwe,
        RateLimitProperties rateLimit,
        ComplianceProperties compliance,
        SessionProperties session,
        ServicesProperties services,
        DownstreamProperties downstream
) {
    public record JwtProperties(String secret, String issuer, Duration accessTokenTtl) {}
    public record SiweProperties(Duration nonceTtl, String domain, String uri) {}
    public record RateLimitProperties(boolean enabled, int requestsPerMinute) {}
    public record ComplianceProperties(
            boolean enabled,
            String policyVersion,
            boolean blockMainlandChina,
            List<String> countryPriority,
            String geoipDatabasePath
    ) {}
    public record SessionProperties(Duration ttl) {}
    public record ServicesProperties(
            ServiceEndpoint marketSchema,
            ServiceEndpoint matching,
            ServiceEndpoint bacp,
            ServiceEndpoint indexer,
            ServiceEndpoint oracleOps
    ) {}
    public record ServiceEndpoint(String baseUrl) {}
    public record DownstreamProperties(int connectTimeoutMs, int readTimeoutMs, int maxRetries, int retryBackoffMs) {}
}
