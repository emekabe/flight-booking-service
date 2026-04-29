package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Flight;
import com.emekachukwulobe.flightbookingservice.domain.FlightInventory;
import com.emekachukwulobe.flightbookingservice.domain.enums.FlightStatus;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateFlightRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.SearchFlightRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.UpdateFlightInventoryRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.FlightResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.PagedResponse;
import com.emekachukwulobe.flightbookingservice.exception.DuplicateResourceException;
import com.emekachukwulobe.flightbookingservice.exception.FlightNotFoundException;
import com.emekachukwulobe.flightbookingservice.mapper.FlightMapper;
import com.emekachukwulobe.flightbookingservice.repository.FlightInventoryRepository;
import com.emekachukwulobe.flightbookingservice.repository.FlightRepository;
import com.emekachukwulobe.flightbookingservice.repository.FlightSpecification;
import com.emekachukwulobe.flightbookingservice.service.FlightService;
import com.emekachukwulobe.flightbookingservice.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final FlightInventoryRepository inventoryRepository;
    private final TenantService tenantService;
    private final FlightMapper flightMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "flights", key = "#tenantId + ':' + #request.origin + ':' + #request.destination + ':' + #request.departureDate + ':' + #request.fareClass + ':' + #pageable.pageNumber")
    public PagedResponse<FlightResponse> searchFlights(SearchFlightRequest request, UUID tenantId, Pageable pageable) {
        var spec = FlightSpecification.buildSearchSpec(
            tenantId,
            request.getOrigin(),
            request.getDestination(),
            request.getDepartureDate(),
            request.getFareClass()
        );
        Page<FlightResponse> page = flightRepository.findAll(spec, pageable).map(flightMapper::toResponse);
        return PagedResponse.from(page);
    }

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public FlightResponse createFlight(CreateFlightRequest request, UUID tenantId) {
        if (flightRepository.existsByFlightNumberAndTenantId(request.getFlightNumber(), tenantId)) {
            throw new DuplicateResourceException("Flight number already exists: " + request.getFlightNumber());
        }

        var tenant = tenantService.getEntityById(tenantId);

        Flight flight = Flight.builder()
            .tenant(tenant)
            .flightNumber(request.getFlightNumber().toUpperCase())
            .origin(request.getOrigin().toUpperCase())
            .destination(request.getDestination().toUpperCase())
            .departureTime(request.getDepartureTime())
            .arrivalTime(request.getArrivalTime())
            .status(FlightStatus.SCHEDULED)
            .build();

        Flight savedFlight = flightRepository.save(flight);

        FlightInventory inventory = FlightInventory.builder()
            .flight(savedFlight)
            .tenantId(tenantId)
            .totalSeats(0)
            .availableSeats(0)
            .build();

        inventoryRepository.save(inventory);
        savedFlight.setInventory(inventory);

        return flightMapper.toResponse(savedFlight);
    }

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public FlightResponse updateFlight(UUID flightId, CreateFlightRequest request, UUID tenantId) {
        Flight flight = findOrThrow(flightId, tenantId);
        flight.setFlightNumber(request.getFlightNumber().toUpperCase());
        flight.setOrigin(request.getOrigin().toUpperCase());
        flight.setDestination(request.getDestination().toUpperCase());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        return flightMapper.toResponse(flightRepository.save(flight));
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlightById(UUID flightId, UUID tenantId) {
        return flightMapper.toResponse(findOrThrow(flightId, tenantId));
    }

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public FlightResponse updateInventory(UUID flightId, UpdateFlightInventoryRequest request, UUID tenantId) {
        FlightInventory inventory = inventoryRepository.findByFlightId(flightId)
            .filter(inv -> inv.getTenantId().equals(tenantId))
            .orElseThrow(() -> new FlightNotFoundException("Inventory not found for flight: " + flightId));

        inventory.setTotalSeats(request.getTotalSeats());
        inventory.setAvailableSeats(request.getTotalSeats());
        inventoryRepository.save(inventory);

        return flightMapper.toResponse(findOrThrow(flightId, tenantId));
    }

    private Flight findOrThrow(UUID flightId, UUID tenantId) {
        return flightRepository.findByIdAndTenantId(flightId, tenantId)
            .orElseThrow(() -> new FlightNotFoundException("Flight not found: " + flightId));
    }
}
