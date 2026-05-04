package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Aircraft;
import com.emekachukwulobe.flightbookingservice.domain.AircraftSeatDefinition;
import com.emekachukwulobe.flightbookingservice.domain.Fare;
import com.emekachukwulobe.flightbookingservice.domain.Flight;
import com.emekachukwulobe.flightbookingservice.domain.FlightInventory;
import com.emekachukwulobe.flightbookingservice.domain.Seat;
import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import com.emekachukwulobe.flightbookingservice.domain.enums.FlightStatus;
import com.emekachukwulobe.flightbookingservice.domain.enums.SeatNamingFormat;
import com.emekachukwulobe.flightbookingservice.domain.enums.SeatStatus;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateFlightRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.FareRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.SearchFlightRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.UpdateFlightInventoryRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.FlightResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.PagedResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.SeatResponse;
import com.emekachukwulobe.flightbookingservice.exception.BookingException;
import com.emekachukwulobe.flightbookingservice.exception.DuplicateResourceException;
import com.emekachukwulobe.flightbookingservice.exception.FlightNotFoundException;
import com.emekachukwulobe.flightbookingservice.mapper.FlightMapper;
import com.emekachukwulobe.flightbookingservice.repository.AircraftRepository;
import com.emekachukwulobe.flightbookingservice.repository.FareRepository;
import com.emekachukwulobe.flightbookingservice.repository.FlightInventoryRepository;
import com.emekachukwulobe.flightbookingservice.repository.FlightRepository;
import com.emekachukwulobe.flightbookingservice.repository.FlightSpecification;
import com.emekachukwulobe.flightbookingservice.repository.SeatRepository;
import com.emekachukwulobe.flightbookingservice.service.FlightService;
import com.emekachukwulobe.flightbookingservice.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final FlightInventoryRepository inventoryRepository;
    private final FareRepository fareRepository;
    private final SeatRepository seatRepository;
    private final AircraftRepository aircraftRepository;
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
        Aircraft aircraft = null;

        if (request.getAircraftId() != null) {
            aircraft = aircraftRepository.findByIdAndTenantId(request.getAircraftId(), tenantId)
                .orElseThrow(() -> new FlightNotFoundException("Aircraft not found: " + request.getAircraftId()));
            if (aircraftRepository.isAircraftBusy(aircraft.getId(), request.getDepartureTime(), request.getArrivalTime())) {
                throw new BookingException("Aircraft " + aircraft.getRegistrationNumber()
                    + " is already assigned to another flight in this time window");
            }
        }

        Flight flight = Flight.builder()
            .tenant(tenant)
            .aircraft(aircraft)
            .flightNumber(request.getFlightNumber().toUpperCase())
            .origin(request.getOrigin().toUpperCase())
            .destination(request.getDestination().toUpperCase())
            .departureTime(request.getDepartureTime())
            .arrivalTime(request.getArrivalTime())
            .status(FlightStatus.SCHEDULED)
            .build();

        Flight savedFlight = flightRepository.save(flight);

        // Persist fares
        if (request.getFares() != null && !request.getFares().isEmpty()) {
            persistFares(savedFlight, tenantId, request.getFares());
        }

        // Generate seats from aircraft layout
        int totalSeats = 0;
        if (aircraft != null) {
            totalSeats = generateSeats(savedFlight, tenantId, aircraft);
        }

        FlightInventory inventory = FlightInventory.builder()
            .flight(savedFlight)
            .tenantId(tenantId)
            .totalSeats(totalSeats)
            .availableSeats(totalSeats)
            .build();

        inventoryRepository.save(inventory);
        savedFlight.setInventory(inventory);

        return flightMapper.toResponse(flightRepository.findById(savedFlight.getId()).orElse(savedFlight));
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

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public FlightResponse upsertFares(UUID flightId, List<FareRequest> fareRequests, UUID tenantId) {
        Flight flight = findOrThrow(flightId, tenantId);
        // Delete existing fares for this flight and tenant before re-creating
        List<Fare> existing = fareRepository.findAllByFlightIdAndTenantId(flightId, tenantId);
        fareRepository.deleteAll(existing);
        persistFares(flight, tenantId, fareRequests);
        return flightMapper.toResponse(findOrThrow(flightId, tenantId));
    }

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public FlightResponse assignAircraft(UUID flightId, UUID aircraftId, UUID tenantId) {
        Flight flight = findOrThrow(flightId, tenantId);
        Aircraft aircraft = aircraftRepository.findByIdAndTenantId(aircraftId, tenantId)
            .orElseThrow(() -> new FlightNotFoundException("Aircraft not found: " + aircraftId));

        if (aircraftRepository.isAircraftBusy(aircraftId, flight.getDepartureTime(), flight.getArrivalTime())) {
            throw new BookingException("Aircraft " + aircraft.getRegistrationNumber()
                + " is already assigned to another flight in this time window");
        }

        // Clear existing seats generated by any previous aircraft assignment
        seatRepository.deleteAll(seatRepository.findAllByFlightId(flightId));

        flight.setAircraft(aircraft);
        flightRepository.save(flight);

        int newTotal = generateSeats(flight, tenantId, aircraft);

        // Sync inventory
        inventoryRepository.findByFlightId(flightId).ifPresent(inv -> {
            inv.setTotalSeats(newTotal);
            inv.setAvailableSeats(newTotal);
            inventoryRepository.save(inv);
        });

        return flightMapper.toResponse(findOrThrow(flightId, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeats(UUID flightId, UUID tenantId) {
        findOrThrow(flightId, tenantId); // security check
        return seatRepository.findAllByFlightId(flightId).stream()
            .map(s -> SeatResponse.builder()
                .id(s.getId())
                .seatNumber(s.getSeatNumber())
                .cabinClass(s.getCabinClass())
                .status(s.getStatus())
                .build())
            .toList();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void persistFares(Flight flight, UUID tenantId, List<FareRequest> fareRequests) {
        List<Fare> fares = fareRequests.stream()
            .map(r -> Fare.builder()
                .flight(flight)
                .tenantId(tenantId)
                .fareClass(r.getFareClass())
                .price(r.getPrice())
                .currency(r.getCurrency().toUpperCase())
                .build())
            .toList();
        fareRepository.saveAll(fares);
    }

    /**
     * Generates Seat records from the aircraft's seat definitions.
     * AIRLINE format: seatNumber = rowNumber + seatPosition (e.g. "12A")
     * SEQUENTIAL format: seatNumber = sequential position (e.g. "1", "2")
     */
    private int generateSeats(Flight flight, UUID tenantId, Aircraft aircraft) {
        List<AircraftSeatDefinition> defs = aircraft.getSeatDefinitions().stream()
            .filter(AircraftSeatDefinition::isActive)
            .toList();

        List<Seat> seats = new ArrayList<>();
        int counter = 1;

        for (AircraftSeatDefinition def : defs) {
            String seatNumber;
            if (aircraft.getSeatNamingFormat() == SeatNamingFormat.AIRLINE) {
                seatNumber = def.getRowNumber() + def.getSeatPosition();
            } else {
                seatNumber = String.valueOf(counter++);
            }
            seats.add(Seat.builder()
                .flight(flight)
                .tenantId(tenantId)
                .seatNumber(seatNumber)
                .cabinClass(def.getCabinClass())
                .status(SeatStatus.AVAILABLE)
                .build());
        }

        seatRepository.saveAll(seats);
        return seats.size();
    }

    private Flight findOrThrow(UUID flightId, UUID tenantId) {
        return flightRepository.findByIdAndTenantId(flightId, tenantId)
            .orElseThrow(() -> new FlightNotFoundException("Flight not found: " + flightId));
    }
}
