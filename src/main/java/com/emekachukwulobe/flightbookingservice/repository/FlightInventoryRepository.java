package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.FlightInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FlightInventoryRepository extends JpaRepository<FlightInventory, UUID> {

    Optional<FlightInventory> findByFlightId(UUID flightId);

    /**
     * Fetches inventory with an optimistic read lock, ensuring the version is
     * checked at commit time to prevent concurrent seat over-booking.
     */
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT fi FROM FlightInventory fi WHERE fi.flight.id = :flightId AND fi.tenantId = :tenantId")
    Optional<FlightInventory> findByFlightIdAndTenantIdWithLock(
        @Param("flightId") UUID flightId,
        @Param("tenantId") UUID tenantId
    );
}
