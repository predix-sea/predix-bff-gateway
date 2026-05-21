package com.predix.bff.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.fppt.jedismock.RedisServer;

@TestConfiguration
public class TestRedisConfig {

    @Bean(destroyMethod = "stop")
    RedisServer redisServer() throws Exception {
        RedisServer server = RedisServer.newRedisServer(0);
        server.start();
        return server;
    }

    @Bean
    @Primary
    RedisConnectionFactory redisConnectionFactory(RedisServer redisServer) {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisServer.getHost(), redisServer.getBindPort());
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    @Primary
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
        return template;
    }
}
