package com.emekachukwulobe.flightbookingservice.dto.response;

import com.emekachukwulobe.flightbookingservice.domain.enums.PaymentMethod;
import com.emekachukwulobe.flightbookingservice.domain.enums.PaymentStatus;
import com.emekachukwulobe.flightbookingservice.domain.enums.ProviderType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    private UUID id;
    private String bookingReference;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentMethod method;
    private ProviderType provider;
    private String providerReference;
    private OffsetDateTime createdAt;
}
