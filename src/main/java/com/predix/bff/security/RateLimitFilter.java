package com.predix.bff.security;

import com.predix.bff.compliance.IpExtractor;
import com.predix.bff.config.PredixProperties;
import com.predix.bff.exception.BffException;
import com.predix.bff.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@Order(10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String KEY_PREFIX = "ratelimit:";
    private final StringRedisTemplate redis;
    private final boolean enabled;
    private final int requestsPerMinute;

    public RateLimitFilter(StringRedisTemplate redis, PredixProperties properties) {
        this.redis = redis;
        this.enabled = properties.rateLimit().enabled();
        this.requestsPerMinute = properties.rateLimit().requestsPerMinute();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }
        String ip = IpExtractor.extractClientIp(request);
        String key = KEY_PREFIX + ip;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, Duration.ofMinutes(1));
        }
        if (count != null && count > requestsPerMinute) {
            throw new BffException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
        chain.doFilter(request, response);
    }
}
