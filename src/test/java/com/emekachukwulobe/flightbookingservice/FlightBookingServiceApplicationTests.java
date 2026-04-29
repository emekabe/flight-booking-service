package com.emekachukwulobe.flightbookingservice;

import org.junit.jupiter.api.Test;

/**
 * Integration context test is skipped in unit test runs — it requires
 * a running PostgreSQL + Redis (provided by Testcontainers in CI).
 * Run with: ./gradlew test -Dspring.profiles.active=integration
 */
class FlightBookingServiceApplicationTests {

    @Test
    void placeholder() {
        // Intentionally empty — full context test requires Testcontainers / live services.
    }
}
