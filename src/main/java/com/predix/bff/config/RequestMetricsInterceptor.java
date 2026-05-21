package com.predix.bff.config;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestMetricsInterceptor implements HandlerInterceptor {

    private final MeterRegistry meterRegistry;

    public RequestMetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String path = request.getRequestURI();
        String status = String.valueOf(response.getStatus());
        meterRegistry.counter("bff_request_total", "path", sanitize(path), "status", status).increment();
    }

    private String sanitize(String path) {
        return path.replaceAll("/[0-9a-fA-F\\-]{8,}", "/{id}");
    }
}
