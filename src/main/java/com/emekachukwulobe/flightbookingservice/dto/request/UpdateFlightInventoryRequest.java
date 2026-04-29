package com.emekachukwulobe.flightbookingservice.dto.request;

import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFlightInventoryRequest {

    @NotNull
    @Positive
    private Integer totalSeats;

    @NotNull
    private FareClass fareClass;
}
