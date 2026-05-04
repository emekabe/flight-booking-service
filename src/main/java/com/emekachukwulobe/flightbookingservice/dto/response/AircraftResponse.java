package com.emekachukwulobe.flightbookingservice.dto.response;

import com.emekachukwulobe.flightbookingservice.domain.enums.SeatNamingFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AircraftResponse {
    private UUID id;
    private String registrationNumber;
    private String model;
    private SeatNamingFormat seatNamingFormat;
    private boolean active;
    private int totalSeats;
    private List<AircraftSeatDefinitionResponse> seatDefinitions;
}
