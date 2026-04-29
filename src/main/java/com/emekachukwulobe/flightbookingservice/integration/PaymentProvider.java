package com.emekachukwulobe.flightbookingservice.integration;

import com.emekachukwulobe.flightbookingservice.domain.enums.ProviderType;

import java.util.Map;

/**
 * Abstraction over external payment providers.
 * Each implementation registers itself with a specific {@link ProviderType}.
 */
public interface PaymentProvider {

    PaymentResult initiatePayment(PaymentRequest request);

    PaymentResult handleCallback(Map<String, String> payload);

    ProviderType providerType();
}
