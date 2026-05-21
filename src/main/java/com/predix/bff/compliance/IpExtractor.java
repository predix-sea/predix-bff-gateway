package com.predix.bff.compliance;

import jakarta.servlet.http.HttpServletRequest;

public final class IpExtractor {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String CF_CONNECTING_IP = "CF-Connecting-IP";

    private IpExtractor() {}

    public static String extractClientIp(HttpServletRequest request) {
        String cf = request.getHeader(CF_CONNECTING_IP);
        if (cf != null && !cf.isBlank()) {
            return cf.trim();
        }
        String xff = request.getHeader(X_FORWARDED_FOR);
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader(X_REAL_IP);
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
