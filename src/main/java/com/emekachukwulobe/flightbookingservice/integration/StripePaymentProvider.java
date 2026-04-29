package com.emekachukwulobe.flightbookingservice.integration;

import com.emekachukwulobe.flightbookingservice.domain.enums.ProviderType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StripePaymentProvider implements PaymentProvider {

    @Override
    public PaymentResult initiatePayment(PaymentRequest request) {
        throw new UnsupportedOperationException("Stripe integration not yet implemented");
    }

    @Override
    public PaymentResult handleCallback(Map<String, String> payload) {
        throw new UnsupportedOperationException("Stripe integration not yet implemented");
    }

    @Override
    public ProviderType providerType() {
        return ProviderType.STRIPE;
    }
}
