package com.ciphermarket.api.health;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        try {
            String pong = redisConnectionFactory.getConnection().ping();
            if ("PONG".equalsIgnoreCase(pong)) {
                return Health.up().withDetail("redis", "connected").build();
            }
            return Health.down().withDetail("redis", pong).build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("redis", "unavailable").build();
        }
    }
}
