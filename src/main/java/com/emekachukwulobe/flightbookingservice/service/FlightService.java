package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.dto.request.CreateFlightRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.FareRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.SearchFlightRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.UpdateFlightInventoryRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.FlightResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.PagedResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.SeatResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FlightService {

    PagedResponse<FlightResponse> searchFlights(SearchFlightRequest request, UUID tenantId, Pageable pageable);

    FlightResponse createFlight(CreateFlightRequest request, UUID tenantId);

    FlightResponse updateFlight(UUID flightId, CreateFlightRequest request, UUID tenantId);

    FlightResponse getFlightById(UUID flightId, UUID tenantId);

    FlightResponse updateInventory(UUID flightId, UpdateFlightInventoryRequest request, UUID tenantId);

    /** Replace all fares on a flight. */
    FlightResponse upsertFares(UUID flightId, List<FareRequest> fareRequests, UUID tenantId);

    /** Assign an aircraft to a flight and auto-generate seats from its layout. */
    FlightResponse assignAircraft(UUID flightId, UUID aircraftId, UUID tenantId);

    /** Returns all seats for a flight. */
    List<SeatResponse> getSeats(UUID flightId, UUID tenantId);
}
