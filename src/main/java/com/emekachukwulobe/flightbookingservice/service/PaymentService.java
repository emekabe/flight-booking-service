package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.dto.request.InitiatePaymentRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.WebhookPayloadRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiatePayment(InitiatePaymentRequest request, UUID tenantId);

    void handleWebhook(WebhookPayloadRequest payload);

    PaymentResponse getPaymentStatus(String bookingReference, UUID tenantId);
}
