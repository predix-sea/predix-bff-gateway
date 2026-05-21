package com.predix.bff.security;

public record SessionUser(
        String walletAddress,
        long chainId,
        String kycStatus
) {
    public boolean isKycApproved() {
        return "APPROVED".equalsIgnoreCase(kycStatus);
    }
}
