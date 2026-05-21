package com.predix.bff.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.predix.bff.config.PredixProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class SessionService {

    private static final String SESSION_PREFIX = "session:";
    private final StringRedisTemplate redis;
    private final Duration ttl;
    private final ObjectMapper objectMapper;

    public SessionService(StringRedisTemplate redis, PredixProperties properties, ObjectMapper objectMapper) {
        this.redis = redis;
        this.ttl = properties.session().ttl();
        this.objectMapper = objectMapper;
    }

    public void saveSession(String sessionId, SessionUser user) {
        try {
            redis.opsForValue().set(SESSION_PREFIX + sessionId, objectMapper.writeValueAsString(user), ttl);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize session", e);
        }
    }

    public Optional<SessionUser> getSession(String sessionId) {
        String json = redis.opsForValue().get(SESSION_PREFIX + sessionId);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, SessionUser.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public void invalidateSession(String sessionId) {
        redis.delete(SESSION_PREFIX + sessionId);
    }
}
