package com.emekachukwulobe.flightbookingservice.integration;

import com.emekachukwulobe.flightbookingservice.domain.enums.PaymentStatus;
import com.emekachukwulobe.flightbookingservice.domain.enums.ProviderType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class TransferPaymentProvider implements PaymentProvider {

    @Override
    public PaymentResult initiatePayment(PaymentRequest request) {
        return new PaymentResult(PaymentStatus.PENDING, UUID.randomUUID().toString(), "Awaiting bank transfer confirmation");
    }

    @Override
    public PaymentResult handleCallback(Map<String, String> payload) {
        return new PaymentResult(PaymentStatus.COMPLETED, payload.get("reference"), "Transfer confirmed");
    }

    @Override
    public ProviderType providerType() {
        return null; // TRANSFER is not a ProviderType — handled directly in PaymentServiceImpl
    }
}
