package com.emekachukwulobe.flightbookingservice.integration;

import com.emekachukwulobe.flightbookingservice.domain.enums.ProviderType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaystackPaymentProvider implements PaymentProvider {

    @Override
    public PaymentResult initiatePayment(PaymentRequest request) {
        throw new UnsupportedOperationException("Paystack integration not yet implemented");
    }

    @Override
    public PaymentResult handleCallback(Map<String, String> payload) {
        throw new UnsupportedOperationException("Paystack integration not yet implemented");
    }

    @Override
    public ProviderType providerType() {
        return ProviderType.PAYSTACK;
    }
}
