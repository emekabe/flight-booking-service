package com.emekachukwulobe.flightbookingservice.dto.request;

import com.emekachukwulobe.flightbookingservice.domain.enums.PaymentMethod;
import com.emekachukwulobe.flightbookingservice.domain.enums.ProviderType;
import jakarta.validation.constraints.NotBlank;
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
public class InitiatePaymentRequest {

    @NotBlank
    private String bookingReference;

    @NotNull
    private PaymentMethod method;

    /** Required when method is PROVIDER. */
    private ProviderType provider;

    @NotBlank
    @Size(min = 3, max = 3, message = "Currency must be a 3-character ISO 4217 code")
    private String currency;
}
