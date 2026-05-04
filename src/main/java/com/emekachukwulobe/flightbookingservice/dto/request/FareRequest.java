package com.emekachukwulobe.flightbookingservice.dto.request;

import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRequest {

    @NotNull
    private FareClass fareClass;

    @NotNull
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    /** ISO 4217 currency code, e.g. NGN, USD. */
    @NotBlank
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO 4217 code")
    private String currency;
}
