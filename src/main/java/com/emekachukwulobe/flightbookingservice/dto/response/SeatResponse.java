package com.emekachukwulobe.flightbookingservice.dto.response;

import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import com.emekachukwulobe.flightbookingservice.domain.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private UUID id;
    private String seatNumber;
    private FareClass cabinClass;
    private SeatStatus status;
}
