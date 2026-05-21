package com.predix.bff.service;

import com.predix.bff.audit.AuditEventType;
import com.predix.bff.audit.AuditRecorder;
import com.predix.bff.config.PredixProperties;
import com.predix.bff.dto.auth.*;
import com.predix.bff.security.*;
import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final NonceService nonceService;
    private final SiweVerifier siweVerifier;
    private final JwtTokenProvider jwtTokenProvider;
    private final SessionService sessionService;
    private final AuditRecorder auditRecorder;
    private final Counter authFailCounter;
    private final PredixProperties.SiweProperties siweProperties;

    public AuthService(NonceService nonceService,
                       SiweVerifier siweVerifier,
                       JwtTokenProvider jwtTokenProvider,
                       SessionService sessionService,
                       AuditRecorder auditRecorder,
                       Counter authFailCounter,
                       PredixProperties properties) {
        this.nonceService = nonceService;
        this.siweVerifier = siweVerifier;
        this.jwtTokenProvider = jwtTokenProvider;
        this.sessionService = sessionService;
        this.auditRecorder = auditRecorder;
        this.authFailCounter = authFailCounter;
        this.siweProperties = properties.siwe();
    }

    public NonceResponse createNonce() {
        String nonce = nonceService.generateNonce();
        String message = buildSiweMessage(nonce);
        return new NonceResponse(nonce, message, siweProperties.domain());
    }

    public AuthTokenResponse verify(SiweVerifyRequest request, String clientIp) {
        try {
            String nonce = siweVerifier.extractNonce(request.message());
            nonceService.consumeNonce(nonce);
            siweVerifier.verify(request.walletAddress(), request.message(), request.signature(), request.chainId());

            String sessionId = jwtTokenProvider.newSessionId();
            SessionUser user = new SessionUser(request.walletAddress().toLowerCase(), request.chainId(), "PENDING");
            sessionService.saveSession(sessionId, user);
            String token = jwtTokenProvider.createToken(request.walletAddress(), request.chainId(), sessionId);

            auditRecorder.log(AuditEventType.AUTH_SUCCESS, request.walletAddress(), clientIp, null,
                    "/api/v1/auth/siwe/verify", "VERIFY", "SUCCESS", null);
            return new AuthTokenResponse(token, "Bearer", sessionId);
        } catch (RuntimeException ex) {
            authFailCounter.increment();
            auditRecorder.log(AuditEventType.AUTH_FAIL, request.walletAddress(), clientIp, null,
                    "/api/v1/auth/siwe/verify", "VERIFY", "FAIL", ex.getMessage());
            throw ex;
        }
    }

    public void logout(String sessionId, String wallet, String clientIp) {
        sessionService.invalidateSession(sessionId);
        auditRecorder.log(AuditEventType.AUTH_LOGOUT, wallet, clientIp, null,
                "/api/v1/auth/logout", "LOGOUT", "SUCCESS", null);
    }

    public MeResponse me(SessionUser user) {
        return new MeResponse(user.walletAddress(), user.chainId(), user.kycStatus());
    }

    private String buildSiweMessage(String nonce) {
        return """
                %s wants you to sign in with your Ethereum account:
                
                predix-bff-gateway
                
                Sign in to PrediX BFF Gateway
                
                URI: %s
                Version: 1
                Chain ID: 1
                Nonce: %s
                Issued At: %s
                """.formatted(siweProperties.domain(), siweProperties.uri(), nonce, java.time.Instant.now());
    }
}
