package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Booking;
import com.emekachukwulobe.flightbookingservice.domain.Payment;
import com.emekachukwulobe.flightbookingservice.domain.enums.BookingStatus;
import com.emekachukwulobe.flightbookingservice.domain.enums.PaymentMethod;
import com.emekachukwulobe.flightbookingservice.domain.enums.PaymentStatus;
import com.emekachukwulobe.flightbookingservice.dto.request.InitiatePaymentRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.WebhookPayloadRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.PaymentResponse;
import com.emekachukwulobe.flightbookingservice.exception.BookingNotFoundException;
import com.emekachukwulobe.flightbookingservice.exception.PaymentException;
import com.emekachukwulobe.flightbookingservice.integration.PaymentProviderRegistry;
import com.emekachukwulobe.flightbookingservice.integration.PaymentRequest;
import com.emekachukwulobe.flightbookingservice.integration.PaymentResult;
import com.emekachukwulobe.flightbookingservice.mapper.PaymentMapper;
import com.emekachukwulobe.flightbookingservice.repository.BookingRepository;
import com.emekachukwulobe.flightbookingservice.repository.PaymentRepository;
import com.emekachukwulobe.flightbookingservice.service.PaymentService;
import com.emekachukwulobe.flightbookingservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final TicketService ticketService;
    private final PaymentProviderRegistry providerRegistry;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, UUID tenantId) {
        Booking booking = bookingRepository.findByBookingReferenceAndTenantId(request.getBookingReference(), tenantId)
            .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + request.getBookingReference()));

        if (booking.getPayment() != null) {
            throw new PaymentException("Payment already initiated for booking: " + request.getBookingReference());
        }

        if (request.getMethod() == PaymentMethod.PROVIDER && request.getProvider() == null) {
            throw new PaymentException("Provider type is required when payment method is PROVIDER");
        }

        PaymentResult result = resolveAndInitiate(request, booking);

        Payment payment = Payment.builder()
            .booking(booking)
            .tenantId(tenantId)
            .amount(booking.getFlight().getFares().stream()
                .findFirst()
                .map(f -> f.getPrice())
                .orElseThrow(() -> new PaymentException("No fare found for booking")))
            .currency(request.getCurrency())
            .status(result.status())
            .method(request.getMethod())
            .provider(request.getProvider())
            .providerReference(result.providerReference())
            .build();

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public void handleWebhook(WebhookPayloadRequest payload) {
        Payment payment = paymentRepository.findByProviderReference(payload.getProviderReference())
            .orElseThrow(() -> new PaymentException("No payment found for reference: " + payload.getProviderReference()));

        // Idempotency — already processed
        if (payment.isCompleted()) {
            log.info("Webhook already processed for reference: {}", payload.getProviderReference());
            return;
        }

        PaymentStatus newStatus = resolveWebhookStatus(payload.getStatus());
        payment.setStatus(newStatus);
        paymentRepository.save(payment);

        if (payment.isCompleted()) {
            confirmBookingFromWebhook(payment.getBooking());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(String bookingReference, UUID tenantId) {
        return paymentRepository.findByBookingBookingReferenceAndTenantId(bookingReference, tenantId)
            .map(paymentMapper::toResponse)
            .orElseThrow(() -> new PaymentException("Payment not found for booking: " + bookingReference));
    }

    private PaymentResult resolveAndInitiate(InitiatePaymentRequest request, Booking booking) {
        if (request.getMethod() == PaymentMethod.PROVIDER) {
            var pr = new PaymentRequest(
                booking.getId(),
                booking.getBookingReference(),
                booking.getFlight().getFares().stream().findFirst()
                    .map(f -> f.getPrice())
                    .orElseThrow(() -> new PaymentException("No fare found")),
                request.getCurrency(),
                request.getProvider()
            );
            return providerRegistry.resolve(request.getProvider()).initiatePayment(pr);
        }
        // CASH / TRANSFER — return PENDING immediately
        return new PaymentResult(PaymentStatus.PENDING, UUID.randomUUID().toString(), request.getMethod().name() + " payment pending");
    }

    private void confirmBookingFromWebhook(Booking booking) {
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpirationTime(null);
        Booking saved = bookingRepository.save(booking);
        ticketService.issueTickets(saved);
        log.info("Booking {} confirmed via webhook", booking.getBookingReference());
    }

    private PaymentStatus resolveWebhookStatus(String status) {
        return switch (status.toUpperCase()) {
            case "SUCCESS", "COMPLETED", "PAID" -> PaymentStatus.COMPLETED;
            case "FAILED", "DECLINED"           -> PaymentStatus.FAILED;
            case "REFUNDED"                     -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
    }
}
