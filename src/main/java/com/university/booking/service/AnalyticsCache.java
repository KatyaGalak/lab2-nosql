package com.university.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsCache {

    public static final String NAME = "orgBookingStats";

    private final CacheManager cacheManager;

    public void invalidateAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    clear();
                }
            });
        } else {
            clear();
        }
    }

    private void clear() {
        try {
            Cache cache = cacheManager.getCache(NAME);
            if (cache != null) {
                cache.clear();
            }
            log.atDebug().addKeyValue("event", "cache_invalidated").addKeyValue("cache", NAME).log("analytics cache cleared");
        } catch (RuntimeException e) {
            log.atWarn()
                    .addKeyValue("event", "cache_error")
                    .addKeyValue("operation", "clear")
                    .addKeyValue("cache", NAME)
                    .log("cache unavailable, invalidation skipped: {}", e.getMessage());
        }
    }
}