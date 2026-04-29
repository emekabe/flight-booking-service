package com.emekachukwulobe.flightbookingservice.dto.response;

import com.emekachukwulobe.flightbookingservice.domain.enums.FlightStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlightResponse {

    private UUID id;
    private String flightNumber;
    private String origin;
    private String destination;
    private OffsetDateTime departureTime;
    private OffsetDateTime arrivalTime;
    private FlightStatus status;
    private Integer totalSeats;
    private Integer availableSeats;
    private List<FareResponse> fares;
    private OffsetDateTime createdAt;
}
