package com.emekachukwulobe.flightbookingservice.dto.request;

import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AircraftSeatDefinitionRequest {

    @NotNull
    @Min(1)
    @Max(100)
    private Integer rowNumber;

    /**
     * Letter for AIRLINE format (e.g. "A", "B").
     * Omit for SEQUENTIAL format.
     */
    @Size(max = 5)
    private String seatPosition;

    @NotNull
    private FareClass cabinClass;

    /** Set to false to mark this position as non-existent (e.g. emergency row gap). */
    @Builder.Default
    private boolean active = true;
}
