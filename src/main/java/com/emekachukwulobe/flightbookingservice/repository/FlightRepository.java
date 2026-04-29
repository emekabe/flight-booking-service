package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Dynamic flight search is handled via {@link FlightSpecification} +
 * {@link JpaSpecificationExecutor} to support optional filter parameters cleanly.
 */
public interface FlightRepository extends JpaRepository<Flight, UUID>, JpaSpecificationExecutor<Flight> {

    Optional<Flight> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByFlightNumberAndTenantId(String flightNumber, UUID tenantId);
}
