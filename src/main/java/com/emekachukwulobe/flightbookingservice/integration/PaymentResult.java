package com.emekachukwulobe.flightbookingservice.integration;

import com.emekachukwulobe.flightbookingservice.domain.enums.PaymentStatus;

public record PaymentResult(
    PaymentStatus status,
    String providerReference,
    String message
) {}
