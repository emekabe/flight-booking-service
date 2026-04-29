package com.emekachukwulobe.flightbookingservice.controller;

import com.emekachukwulobe.flightbookingservice.dto.request.InitiatePaymentRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.WebhookPayloadRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.ApiResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.PaymentResponse;
import com.emekachukwulobe.flightbookingservice.security.SecurityUtils;
import com.emekachukwulobe.flightbookingservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment initiation and webhook processing")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Initiate payment", description = "Initiates a payment for a booking. For PROVIDER method, delegates to the external payment gateway.")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        PaymentResponse response = paymentService.initiatePayment(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Payment initiated", response));
    }

    @GetMapping("/{bookingReference}")
    @Operation(summary = "Get payment status", description = "Returns the current payment status for a booking reference.")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(
            @PathVariable String bookingReference) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentStatus(bookingReference, tenantId)));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Payment webhook", description = "Receives payment status callbacks from external providers. No authentication required. Provider reference is used for idempotency.")
    public ResponseEntity<Void> handleWebhook(@Valid @RequestBody WebhookPayloadRequest payload) {
        paymentService.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
