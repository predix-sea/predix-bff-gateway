package com.predix.bff.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SiweVerifyRequest(
        @NotBlank @Pattern(regexp = "(?i)^0x[a-fA-F0-9]{40}$") String walletAddress,
        @NotBlank String message,
        @NotBlank String signature,
        @NotNull Long chainId
) {}
