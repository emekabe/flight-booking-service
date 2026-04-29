package com.emekachukwulobe.flightbookingservice.integration;

import com.emekachukwulobe.flightbookingservice.domain.enums.PaymentStatus;
import com.emekachukwulobe.flightbookingservice.domain.enums.ProviderType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class CashPaymentProvider implements PaymentProvider {

    @Override
    public PaymentResult initiatePayment(PaymentRequest request) {
        return new PaymentResult(PaymentStatus.PENDING, UUID.randomUUID().toString(), "Awaiting cash collection");
    }

    @Override
    public PaymentResult handleCallback(Map<String, String> payload) {
        return new PaymentResult(PaymentStatus.COMPLETED, payload.get("reference"), "Cash payment confirmed");
    }

    @Override
    public ProviderType providerType() {
        return null; // CASH is not a ProviderType — handled directly in PaymentServiceImpl
    }
}
