package com.emekachukwulobe.flightbookingservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlightRequest {

    @NotBlank
    @Size(max = 20)
    private String flightNumber;

    /** IATA airport code. */
    @NotBlank
    @Size(min = 3, max = 10)
    private String origin;

    /** IATA airport code. */
    @NotBlank
    @Size(min = 3, max = 10)
    private String destination;

    @NotNull
    private OffsetDateTime departureTime;

    @NotNull
    private OffsetDateTime arrivalTime;

    /** Optional: aircraft to assign. Seats are auto-generated from aircraft layout. */
    private UUID aircraftId;

    /** Optional: initial fares to create with the flight. Can also be set via PUT /{id}/fares. */
    @Valid
    @Builder.Default
    private List<FareRequest> fares = new ArrayList<>();
}
