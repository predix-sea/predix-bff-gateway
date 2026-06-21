package com.predix.bff.integration;

import com.predix.bff.security.JwtTokenProvider;
import com.predix.bff.security.SessionService;
import com.predix.bff.security.SessionUser;
import com.predix.bff.support.TestRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class BffIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private SessionService sessionService;

    @Test
    void nonceEndpointPublic() throws Exception {
        mockMvc.perform(get("/api/v1/auth/siwe/nonce")
                        .param("address", "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.nonce").isNotEmpty())
                .andExpect(jsonPath("$.data.message", containsString("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd")));
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cnIpBlocked() throws Exception {
        String sessionId = jwtTokenProvider.newSessionId();
        sessionService.saveSession(sessionId, new SessionUser("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd", 1L, "APPROVED"));
        String token = jwtTokenProvider.createToken("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd", 1L, sessionId);

        mockMvc.perform(get("/api/v1/markets")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Forwarded-For", "203.0.113.50"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMPLIANCE_CN_BLOCKED"));
    }

    @Test
    void authenticatedMeWithValidSession() throws Exception {
        String sessionId = jwtTokenProvider.newSessionId();
        sessionService.saveSession(sessionId, new SessionUser("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd", 1L, "PENDING"));
        String token = jwtTokenProvider.createToken("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd", 1L, sessionId);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Forwarded-For", "8.8.8.8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.walletAddress").exists());
    }

    @Test
    void custodyRequiresKyc() throws Exception {
        String sessionId = jwtTokenProvider.newSessionId();
        sessionService.saveSession(sessionId, new SessionUser("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd", 1L, "PENDING"));
        String token = jwtTokenProvider.createToken("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd", 1L, sessionId);

        mockMvc.perform(post("/api/v1/custody/deposits")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Forwarded-For", "8.8.8.8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMPLIANCE_KYC_REQUIRED"));
    }

    @Test
    void marketsListPublicWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/markets")
                        .header("X-Forwarded-For", "8.8.8.8"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void downstreamUnavailableMapsError() throws Exception {
        String sessionId = jwtTokenProvider.newSessionId();
        sessionService.saveSession(sessionId, new SessionUser("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd", 1L, "APPROVED"));
        String token = jwtTokenProvider.createToken("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd", 1L, sessionId);

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Forwarded-For", "8.8.8.8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marketId\":\"m1\",\"side\":\"BUY\"}"))
                .andExpect(status().isBadGateway());
    }
}
