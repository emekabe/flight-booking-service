package com.emekachukwulobe.flightbookingservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed bindings for the {@code app.*} configuration namespace defined in application.yml.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Booking booking = new Booking();
    private final Scheduler scheduler = new Scheduler();
    private final DataInitializer dataInitializer = new DataInitializer();

    @Getter
    @Setter
    public static class Booking {
        private int expiryMinutes = 30;
    }

    @Getter
    @Setter
    public static class Scheduler {
        private String bookingExpiryCron = "0 * * * * *";
        private String cacheRefreshCron  = "0 0/15 * * * *";
    }

    @Getter
    @Setter
    public static class DataInitializer {
        private String defaultTenantCode;
        private String defaultTenantName;
        private String defaultAdminUsername;
        private String defaultAdminPassword;
    }
}
