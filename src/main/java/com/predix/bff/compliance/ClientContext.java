package com.predix.bff.compliance;

public record ClientContext(
        String clientIp,
        String countryCode,
        String countryFromHeader,
        String policyVersion
) {}
