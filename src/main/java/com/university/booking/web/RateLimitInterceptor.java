package com.university.booking.web;

import com.university.booking.enums.RedisKeyPrefix;
import com.university.booking.service.RateLimiterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final RateLimiterService rateLimiterService;

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             @NonNull Object handler) {

        Object personId = request.getAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE);
        String key = personId != null
                ? RedisKeyPrefix.RATE_LIMIT_PERSON.getKey(personId.toString())
                : RedisKeyPrefix.RATE_LIMIT_IP.getKey(request.getRemoteAddr());

        long count = rateLimiterService.hit(key, WINDOW);
        response.setHeader("RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader("RateLimit-Remaining", String.valueOf(Math.max(0, requestsPerMinute - count)));

        if (count > requestsPerMinute) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit of %d requests per minute exceeded".formatted(requestsPerMinute));
        }

        return true;
    }
}
