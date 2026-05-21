package com.predix.bff.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    AUTH_INVALID_SIGNATURE("AUTH_INVALID_SIGNATURE", "Invalid SIWE signature", HttpStatus.UNAUTHORIZED),
    AUTH_NONCE_EXPIRED("AUTH_NONCE_EXPIRED", "Nonce expired or already used", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_TOKEN("AUTH_INVALID_TOKEN", "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    AUTH_UNAUTHORIZED("AUTH_UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED),
    COMPLIANCE_CN_BLOCKED("COMPLIANCE_CN_BLOCKED", "Access from mainland China is not permitted", HttpStatus.FORBIDDEN),
    COMPLIANCE_COUNTRY_BLOCKED("COMPLIANCE_COUNTRY_BLOCKED", "Access from your region is not permitted", HttpStatus.FORBIDDEN),
    COMPLIANCE_KYC_REQUIRED("COMPLIANCE_KYC_REQUIRED", "KYC verification required for this action", HttpStatus.FORBIDDEN),
    DOWNSTREAM_TIMEOUT("DOWNSTREAM_TIMEOUT", "Downstream service timeout", HttpStatus.GATEWAY_TIMEOUT),
    DOWNSTREAM_UNAVAILABLE("DOWNSTREAM_UNAVAILABLE", "Downstream service unavailable", HttpStatus.BAD_GATEWAY),
    CUSTODY_PATH_VIOLATION("CUSTODY_PATH_VIOLATION", "Fund operations must go through BACP only", HttpStatus.FORBIDDEN),
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed", HttpStatus.BAD_REQUEST),
    NOT_FOUND("NOT_FOUND", "Resource not found", HttpStatus.NOT_FOUND),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
