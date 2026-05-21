package com.predix.bff.dto.auth;

public record MeResponse(String walletAddress, long chainId, String kycStatus) {}
