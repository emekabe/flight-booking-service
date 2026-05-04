package com.emekachukwulobe.flightbookingservice.dto.response;

import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AircraftSeatDefinitionResponse {
    private UUID id;
    private int rowNumber;
    private String seatPosition;
    private FareClass cabinClass;
    private boolean active;
}
