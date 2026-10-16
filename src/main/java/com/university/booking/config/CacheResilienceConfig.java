package com.university.booking.config;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CacheResilienceConfig implements CachingConfigurer {

    @Override
    public CacheErrorHandler errorHandler() {
        return new FallbackToDatabaseCacheErrorHandler();
    }

    static class FallbackToDatabaseCacheErrorHandler implements CacheErrorHandler {

        @Override
        public void handleCacheGetError(@Nonnull RuntimeException e,
                                        @Nonnull Cache cache,
                                        @Nonnull Object key) {
            warn("get", cache, key, e);
        }

        @Override
        public void handleCachePutError(@Nonnull RuntimeException e,
                                        @Nonnull Cache cache,
                                        @Nonnull Object key,
                                        Object value) {
            warn("put", cache, key, e);
        }

        @Override
        public void handleCacheEvictError(@Nonnull RuntimeException e,
                                          @Nonnull Cache cache,
                                          @Nonnull Object key) {
            warn("evict", cache, key, e);
        }

        @Override
        public void handleCacheClearError(@Nonnull RuntimeException e,
                                          @Nonnull Cache cache) {
            warn("clear", cache, null, e);
        }

        private void warn(String operation, Cache cache, Object key, RuntimeException e) {

            log.atWarn()
                    .addKeyValue("event", "cache_error")
                    .addKeyValue("operation", operation)
                    .addKeyValue("cache", cache.getName())
                    .addKeyValue("key", String.valueOf(key))
                    .addKeyValue("error", e.getClass().getSimpleName())
                    .log("cache unavailable, continuing without it: {}", e.getMessage());
        }
    }
}
