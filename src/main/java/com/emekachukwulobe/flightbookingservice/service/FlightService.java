package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.dto.request.CreateFlightRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.SearchFlightRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.UpdateFlightInventoryRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.FlightResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FlightService {

    PagedResponse<FlightResponse> searchFlights(SearchFlightRequest request, UUID tenantId, Pageable pageable);

    FlightResponse createFlight(CreateFlightRequest request, UUID tenantId);

    FlightResponse updateFlight(UUID flightId, CreateFlightRequest request, UUID tenantId);

    FlightResponse getFlightById(UUID flightId, UUID tenantId);

    FlightResponse updateInventory(UUID flightId, UpdateFlightInventoryRequest request, UUID tenantId);
}
