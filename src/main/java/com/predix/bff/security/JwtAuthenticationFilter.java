package com.predix.bff.security;

import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.micrometer.core.instrument.Counter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SessionService sessionService;
    private final Counter authFailCounter;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   SessionService sessionService,
                                   Counter authFailCounter) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.sessionService = sessionService;
        this.authFailCounter = authFailCounter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                Claims claims = jwtTokenProvider.parseToken(token);
                String sessionId = claims.getId();
                SessionUser user = sessionService.getSession(sessionId)
                        .orElseThrow(() -> new BffException(ErrorCode.AUTH_INVALID_TOKEN));
                AuthenticatedUserHolder.set(user);
                var auth = new UsernamePasswordAuthenticationToken(
                        user.walletAddress(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);
                request.setAttribute("sessionId", sessionId);
            }
            chain.doFilter(request, response);
        } catch (BffException ex) {
            authFailCounter.increment();
            throw ex;
        } finally {
            AuthenticatedUserHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
