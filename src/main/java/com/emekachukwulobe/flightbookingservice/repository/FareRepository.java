package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.Fare;
import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FareRepository extends JpaRepository<Fare, UUID> {

    List<Fare> findAllByFlightIdAndTenantId(UUID flightId, UUID tenantId);

    Optional<Fare> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Fare> findByFlightIdAndFareClassAndTenantId(UUID flightId, FareClass fareClass, UUID tenantId);
}
