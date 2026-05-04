package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.domain.Aircraft;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateAircraftRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.AircraftResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AircraftService {
    AircraftResponse createAircraft(CreateAircraftRequest request, UUID tenantId);
    AircraftResponse getAircraft(UUID id, UUID tenantId);
    Page<AircraftResponse> listAircraft(UUID tenantId, Pageable pageable);
    AircraftResponse updateAircraft(UUID id, CreateAircraftRequest request, UUID tenantId);
    /** Returns the Aircraft entity. For internal use by FlightService. */
    Aircraft getEntityById(UUID id, UUID tenantId);
}
