package com.emekachukwulobe.flightbookingservice.dto.request;

import com.emekachukwulobe.flightbookingservice.domain.enums.SeatNamingFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAircraftRequest {

    /** e.g. NG-ABC, 5N-AAA */
    @NotBlank
    @Size(max = 20)
    private String registrationNumber;

    /** e.g. Boeing 737-800 */
    @NotBlank
    @Size(max = 100)
    private String model;

    @NotNull
    private SeatNamingFormat seatNamingFormat;

    /** Full seat layout. Each entry represents one physical seat position. */
    @Valid
    @NotEmpty(message = "Aircraft must have at least one seat definition")
    private List<AircraftSeatDefinitionRequest> seatDefinitions;
}
