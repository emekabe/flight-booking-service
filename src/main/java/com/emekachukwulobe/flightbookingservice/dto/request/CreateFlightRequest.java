package com.emekachukwulobe.flightbookingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

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
}
