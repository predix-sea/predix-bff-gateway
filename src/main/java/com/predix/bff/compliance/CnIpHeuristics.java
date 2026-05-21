package com.predix.bff.compliance;

import java.util.Optional;

/**
 * Fallback heuristic for CN detection when GeoIP DB is unavailable.
 * Uses well-known mainland China public IP ranges (subset for dev/test).
 */
public final class CnIpHeuristics {

    private CnIpHeuristics() {}

    public static Optional<String> resolveCountry(String ip) {
        if (ip == null) {
            return Optional.empty();
        }
        // Test hook: explicit CN simulation IPs
        if (ip.startsWith("203.0.113.") || ip.equals("114.114.114.114")) {
            return Optional.of("CN");
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return Optional.empty();
        }
        try {
            int a = Integer.parseInt(parts[0]);
            int b = Integer.parseInt(parts[1]);
            // China Telecom / Unicom / Mobile common ranges (simplified)
            if (a == 58 || a == 59 || a == 60 || a == 61) return Optional.of("CN");
            if (a == 111 || a == 112 || a == 113 || a == 114 || a == 115) return Optional.of("CN");
            if (a == 116 || a == 117 || a == 118 || a == 119 || a == 120) return Optional.of("CN");
            if (a == 121 || a == 122 || a == 123 || a == 124 || a == 125) return Optional.of("CN");
            if (a == 183 && b >= 0 && b <= 255) return Optional.of("CN");
            if (a == 202 && b >= 96 && b <= 127) return Optional.of("CN");
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
