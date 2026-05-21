package com.predix.bff.dto.auth;

public record NonceResponse(String nonce, String message, String domain) {}
