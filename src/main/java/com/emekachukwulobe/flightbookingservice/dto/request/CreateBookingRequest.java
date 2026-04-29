package com.emekachukwulobe.flightbookingservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CreateBookingRequest {

    @NotNull
    private UUID flightId;

    @NotNull
    private UUID fareId;

    @Valid
    @NotEmpty(message = "At least one passenger is required")
    private List<PassengerRequest> passengers;
}
