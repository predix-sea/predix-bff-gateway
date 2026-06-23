package com.predix.bff.controller;

import com.predix.bff.compliance.IpExtractor;
import com.predix.bff.dto.auth.*;
import com.predix.bff.security.AuthenticatedUserHolder;
import com.predix.bff.security.SessionUser;
import com.predix.bff.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/siwe/nonce")
    public NonceResponse nonce(
            @RequestParam
            @NotBlank
            @Pattern(regexp = "(?i)^0x[a-fA-F0-9]{40}$")
            String address) {
        return authService.createNonce(address);
    }

    @PostMapping("/siwe/verify")
    public AuthTokenResponse verify(@Valid @RequestBody SiweVerifyRequest request, HttpServletRequest httpRequest) {
        String ip = IpExtractor.extractClientIp(httpRequest);
        return authService.verify(request, ip);
    }

    @PostMapping("/logout")
    public Void logout(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        SessionUser user = AuthenticatedUserHolder.get();
        String wallet = user != null ? user.walletAddress() : null;
        authService.logout(sessionId, wallet, IpExtractor.extractClientIp(request));
        return null;
    }

    @GetMapping("/me")
    public MeResponse me() {
        SessionUser user = AuthenticatedUserHolder.get();
        return authService.me(user);
    }
}
