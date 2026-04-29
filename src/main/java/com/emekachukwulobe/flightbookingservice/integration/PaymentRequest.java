package com.emekachukwulobe.flightbookingservice.integration;

import com.emekachukwulobe.flightbookingservice.domain.enums.ProviderType;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
    UUID bookingId,
    String bookingReference,
    BigDecimal amount,
    String currency,
    ProviderType provider
) {}
