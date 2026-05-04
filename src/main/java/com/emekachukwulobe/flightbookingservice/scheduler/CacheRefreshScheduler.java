package com.emekachukwulobe.flightbookingservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically evicts the flights cache to ensure stale search results are refreshed.
 * Cron is configured via {@code app.scheduler.cache-refresh-cron}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshScheduler {

    private final CacheManager cacheManager;

    @Scheduled(cron = "${app.scheduler.cache-refresh-cron}")
    public void refreshFlightCache() {
        var cache = cacheManager.getCache("flights");
        if (cache != null) {
            cache.clear();
            log.debug("Flights cache evicted by scheduled refresh");
        }
    }
}
