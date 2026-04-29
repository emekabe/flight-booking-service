package com.emekachukwulobe.flightbookingservice.config;

import com.emekachukwulobe.flightbookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private final BookingService bookingService;

    /**
     * Runs on the cron defined in {@code app.scheduler.booking-expiry-cron} (default: every minute).
     * Cancels all PENDING bookings whose expiration_time has passed and releases their reserved seats.
     */
    @Scheduled(cron = "${app.scheduler.booking-expiry-cron:0 * * * * *}")
    public void expireBookings() {
        log.debug("BookingExpiryScheduler: checking for stale bookings");
        bookingService.expireStaleBookings();
    }
}
