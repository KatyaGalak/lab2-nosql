package com.university.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class RateLimiterService {

    private static final RedisScript<Long> INCREMENT_IN_WINDOW = RedisScript.of("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 or redis.call('TTL', KEYS[1]) == -1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    public long hit(String key, Duration window) {
        try {
            Long count = stringRedisTemplate.execute(INCREMENT_IN_WINDOW, List.of(key),
                    String.valueOf(window.toSeconds()));

            return count != null ? count : 0;

        } catch (DataAccessException e) {
            log.atWarn()
                    .addKeyValue("event", "rate_limiter_unavailable")
                    .addKeyValue("error", e.getClass().getSimpleName())
                    .log("rate limiter unavailable, request allowed: {}", e.getMessage());
            return 0;
        }
    }
}