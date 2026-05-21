package com.predix.bff.compliance;

import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import com.predix.bff.security.AuthenticatedUserHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.predix.bff.config.TraceIdFilter;
import com.predix.bff.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(20)
public class ComplianceFilter extends OncePerRequestFilter {

    private final ComplianceService complianceService;
    private final ObjectMapper objectMapper;

    public ComplianceFilter(ComplianceService complianceService, ObjectMapper objectMapper) {
        this.complianceService = complianceService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/api-docs")
                || path.startsWith("/api/v1/auth/siwe/nonce") || path.equals("/api/v1/system/dependencies/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String ip = IpExtractor.extractClientIp(request);
        String countryHeader = request.getHeader("X-Country-Code");
        ClientContext ctx = complianceService.buildContext(ip, countryHeader);
        request.setAttribute("clientContext", ctx);
        try {
            complianceService.enforceAccess(ctx, request.getRequestURI(), AuthenticatedUserHolder.get());
            chain.doFilter(request, response);
        } catch (BffException ex) {
            response.setStatus(ex.getErrorCode().getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    ApiResponse.error(ex.getErrorCode().getCode(), ex.getMessage(), TraceIdFilter.currentTraceId()));
        }
    }
}
