package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.Aircraft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AircraftRepository extends JpaRepository<Aircraft, UUID> {

    Page<Aircraft> findAllByTenantId(UUID tenantId, Pageable pageable);

    Optional<Aircraft> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByRegistrationNumberAndTenantId(String registrationNumber, UUID tenantId);

    /**
     * Returns true if the aircraft is already assigned to a flight whose time window
     * overlaps with the requested departure/arrival window.
     */
    @Query("""
        SELECT COUNT(f) > 0 FROM Flight f
        WHERE f.aircraft.id = :aircraftId
          AND f.status <> 'CANCELLED'
          AND f.departureTime < :arrivalTime
          AND f.arrivalTime > :departureTime
    """)
    boolean isAircraftBusy(
        @Param("aircraftId") UUID aircraftId,
        @Param("departureTime") OffsetDateTime departureTime,
        @Param("arrivalTime") OffsetDateTime arrivalTime
    );
}
