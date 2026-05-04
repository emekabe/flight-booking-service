package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Aircraft;
import com.emekachukwulobe.flightbookingservice.domain.AircraftSeatDefinition;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateAircraftRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.AircraftResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.AircraftSeatDefinitionResponse;
import com.emekachukwulobe.flightbookingservice.exception.DuplicateResourceException;
import com.emekachukwulobe.flightbookingservice.repository.AircraftRepository;
import com.emekachukwulobe.flightbookingservice.service.AircraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AircraftServiceImpl implements AircraftService {

    private final AircraftRepository aircraftRepository;

    @Override
    @Transactional
    public AircraftResponse createAircraft(CreateAircraftRequest request, UUID tenantId) {
        if (aircraftRepository.existsByRegistrationNumberAndTenantId(request.getRegistrationNumber(), tenantId)) {
            throw new DuplicateResourceException("Aircraft already exists: " + request.getRegistrationNumber());
        }
        Aircraft aircraft = Aircraft.builder()
            .tenantId(tenantId)
            .registrationNumber(request.getRegistrationNumber().toUpperCase())
            .model(request.getModel())
            .seatNamingFormat(request.getSeatNamingFormat())
            .active(true)
            .build();

        int[] sequentialCounter = {1};
        List<AircraftSeatDefinition> definitions = request.getSeatDefinitions().stream()
            .map(def -> {
                String resolvedPosition = def.getSeatPosition();
                // For SEQUENTIAL format, position is the counter value
                if (aircraft.getSeatNamingFormat().name().equals("SEQUENTIAL")) {
                    resolvedPosition = String.valueOf(sequentialCounter[0]++);
                }
                return AircraftSeatDefinition.builder()
                    .aircraft(aircraft)
                    .rowNumber(def.getRowNumber())
                    .seatPosition(resolvedPosition)
                    .cabinClass(def.getCabinClass())
                    .active(def.isActive())
                    .build();
            })
            .toList();

        aircraft.getSeatDefinitions().addAll(definitions);
        Aircraft saved = aircraftRepository.save(aircraft);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AircraftResponse getAircraft(UUID id, UUID tenantId) {
        return toResponse(getEntityById(id, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AircraftResponse> listAircraft(UUID tenantId, Pageable pageable) {
        return aircraftRepository.findAllByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public AircraftResponse updateAircraft(UUID id, CreateAircraftRequest request, UUID tenantId) {
        Aircraft aircraft = getEntityById(id, tenantId);
        aircraft.setModel(request.getModel());
        aircraft.setSeatNamingFormat(request.getSeatNamingFormat());
        aircraft.getSeatDefinitions().clear();

        int[] sequentialCounter = {1};
        List<AircraftSeatDefinition> definitions = request.getSeatDefinitions().stream()
            .map(def -> {
                String resolvedPosition = def.getSeatPosition();
                if (aircraft.getSeatNamingFormat().name().equals("SEQUENTIAL")) {
                    resolvedPosition = String.valueOf(sequentialCounter[0]++);
                }
                return AircraftSeatDefinition.builder()
                    .aircraft(aircraft)
                    .rowNumber(def.getRowNumber())
                    .seatPosition(resolvedPosition)
                    .cabinClass(def.getCabinClass())
                    .active(def.isActive())
                    .build();
            })
            .toList();

        aircraft.getSeatDefinitions().addAll(definitions);
        return toResponse(aircraftRepository.save(aircraft));
    }

    @Override
    @Transactional(readOnly = true)
    public Aircraft getEntityById(UUID id, UUID tenantId) {
        return aircraftRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new com.emekachukwulobe.flightbookingservice.exception.FlightNotFoundException(
                "Aircraft not found: " + id));
    }

    private AircraftResponse toResponse(Aircraft aircraft) {
        int activeSeats = (int) aircraft.getSeatDefinitions().stream()
            .filter(AircraftSeatDefinition::isActive)
            .count();

        List<AircraftSeatDefinitionResponse> defResponses = aircraft.getSeatDefinitions().stream()
            .map(d -> AircraftSeatDefinitionResponse.builder()
                .id(d.getId())
                .rowNumber(d.getRowNumber())
                .seatPosition(d.getSeatPosition())
                .cabinClass(d.getCabinClass())
                .active(d.isActive())
                .build())
            .toList();

        return AircraftResponse.builder()
            .id(aircraft.getId())
            .registrationNumber(aircraft.getRegistrationNumber())
            .model(aircraft.getModel())
            .seatNamingFormat(aircraft.getSeatNamingFormat())
            .active(aircraft.isActive())
            .totalSeats(activeSeats)
            .seatDefinitions(defResponses)
            .build();
    }
}
