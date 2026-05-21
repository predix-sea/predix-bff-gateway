package com.predix.bff.support;

import com.github.fppt.jedismock.RedisServer;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public final class TestRedisSupport {

    private TestRedisSupport() {}

    public static StringRedisTemplate createTemplate() {
        try {
            RedisServer server = RedisServer.newRedisServer(0);
            server.start();
            LettuceConnectionFactory factory = new LettuceConnectionFactory(server.getHost(), server.getBindPort());
            factory.afterPropertiesSet();
            StringRedisTemplate template = new StringRedisTemplate(factory);
            template.afterPropertiesSet();
            return template;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start jedis-mock", e);
        }
    }
}
