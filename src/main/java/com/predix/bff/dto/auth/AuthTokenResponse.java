package com.predix.bff.dto.auth;

public record AuthTokenResponse(String accessToken, String tokenType, String sessionId) {}
