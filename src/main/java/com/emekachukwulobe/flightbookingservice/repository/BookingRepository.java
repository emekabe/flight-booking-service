package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.Booking;
import com.emekachukwulobe.flightbookingservice.domain.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingReferenceAndTenantId(String bookingReference, UUID tenantId);

    Page<Booking> findAllByTenantId(UUID tenantId, Pageable pageable);

    /**
     * Used by the expiry scheduler to locate and cancel stale PENDING bookings.
     * Fetches passengers eagerly to avoid N+1 when releasing seats.
     */
    @Query("""
        SELECT b FROM Booking b
        LEFT JOIN FETCH b.passengers
        WHERE b.status = :status
        AND b.expirationTime < :now
        """)
    List<Booking> findExpiredBookings(
        @Param("status") BookingStatus status,
        @Param("now") OffsetDateTime now
    );

    /**
     * Counts active (non-cancelled) bookings for a flight — used for inventory auditing.
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.flight.id = :flightId
        AND b.tenantId = :tenantId
        AND b.status <> com.emekachukwulobe.flightbookingservice.domain.enums.BookingStatus.CANCELLED
        """)
    long countActiveBookingsByFlight(@Param("flightId") UUID flightId, @Param("tenantId") UUID tenantId);
}
