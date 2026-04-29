package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.*;
import com.emekachukwulobe.flightbookingservice.domain.enums.*;
import com.emekachukwulobe.flightbookingservice.dto.request.InitiatePaymentRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.WebhookPayloadRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.PaymentResponse;
import com.emekachukwulobe.flightbookingservice.exception.*;
import com.emekachukwulobe.flightbookingservice.integration.PaymentProviderRegistry;
import com.emekachukwulobe.flightbookingservice.mapper.PaymentMapper;
import com.emekachukwulobe.flightbookingservice.repository.BookingRepository;
import com.emekachukwulobe.flightbookingservice.repository.PaymentRepository;
import com.emekachukwulobe.flightbookingservice.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl unit tests")
class PaymentServiceImplTest {

    @Mock PaymentRepository       paymentRepository;
    @Mock BookingRepository       bookingRepository;
    @Mock TicketService           ticketService;
    @Mock PaymentProviderRegistry providerRegistry;
    @Mock PaymentMapper           paymentMapper;

    @InjectMocks PaymentServiceImpl paymentService;

    UUID tenantId, bookingId;
    Booking booking;
    Fare    fare;

    @BeforeEach
    void setUp() {
        tenantId  = UUID.randomUUID();
        bookingId = UUID.randomUUID();

        Tenant tenant = Tenant.builder().name("Air Test").code("AT").build();
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        fare = Fare.builder().tenantId(tenantId).fareClass(FareClass.ECONOMY)
                .price(BigDecimal.valueOf(200)).currency("NGN").build();

        Flight flight = Flight.builder().tenant(tenant).flightNumber("AT002")
                .origin("LOS").destination("PHC")
                .departureTime(OffsetDateTime.now().plusDays(2))
                .arrivalTime(OffsetDateTime.now().plusDays(2).plusHours(1))
                .status(FlightStatus.SCHEDULED).build();
        ReflectionTestUtils.setField(flight, "id", UUID.randomUUID());
        flight.getFares().add(fare);

        User user = User.builder().tenant(tenant).username("agent2")
                .passwordHash("hash").role(UserRole.AGENT).build();

        booking = Booking.builder().tenantId(tenantId).bookingReference("XY1234")
                .flight(flight).user(user).status(BookingStatus.PENDING).build();
        ReflectionTestUtils.setField(booking, "id", bookingId);
    }

    @Test
    @DisplayName("initiatePayment — CASH method creates PENDING payment")
    void initiatePayment_cash_success() {
        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .bookingReference("XY1234").method(PaymentMethod.CASH).currency("NGN").build();

        when(bookingRepository.findByBookingReferenceAndTenantId("XY1234", tenantId))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toResponse(any())).thenReturn(
                PaymentResponse.builder().status(PaymentStatus.PENDING).build());

        PaymentResponse result = paymentService.initiatePayment(req, tenantId);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(paymentRepository).save(argThat(p -> p.getMethod() == PaymentMethod.CASH
                && p.getStatus() == PaymentStatus.PENDING));
    }

    @Test
    @DisplayName("initiatePayment — throws BookingNotFoundException for unknown reference")
    void initiatePayment_bookingNotFound() {
        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .bookingReference("UNKNOWN").method(PaymentMethod.CASH).currency("NGN").build();

        when(bookingRepository.findByBookingReferenceAndTenantId("UNKNOWN", tenantId))
                .thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> paymentService.initiatePayment(req, tenantId));
    }

    @Test
    @DisplayName("initiatePayment — throws PaymentException when payment already exists")
    void initiatePayment_alreadyInitiated() {
        Payment existing = Payment.builder().booking(booking).tenantId(tenantId)
                .amount(BigDecimal.valueOf(200)).currency("NGN")
                .status(PaymentStatus.PENDING).method(PaymentMethod.CASH).build();
        booking.setPayment(existing);

        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .bookingReference("XY1234").method(PaymentMethod.CASH).currency("NGN").build();

        when(bookingRepository.findByBookingReferenceAndTenantId("XY1234", tenantId))
                .thenReturn(Optional.of(booking));

        assertThrows(PaymentException.class, () -> paymentService.initiatePayment(req, tenantId));
    }

    @Test
    @DisplayName("handleWebhook — SUCCESS status confirms booking and issues tickets")
    void handleWebhook_success_completesBooking() {
        Payment payment = Payment.builder().booking(booking).tenantId(tenantId)
                .amount(BigDecimal.valueOf(200)).currency("NGN")
                .status(PaymentStatus.PENDING).method(PaymentMethod.PROVIDER)
                .providerReference("REF-001").build();

        WebhookPayloadRequest payload = WebhookPayloadRequest.builder()
                .providerReference("REF-001").provider("STRIPE").status("SUCCESS").build();

        when(paymentRepository.findByProviderReference("REF-001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);
        when(bookingRepository.save(any())).thenReturn(booking);

        paymentService.handleWebhook(payload);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(ticketService).issueTickets(booking);
    }

    @Test
    @DisplayName("handleWebhook — idempotent when payment already COMPLETED")
    void handleWebhook_idempotent() {
        Payment payment = Payment.builder().booking(booking).tenantId(tenantId)
                .amount(BigDecimal.valueOf(200)).currency("NGN")
                .status(PaymentStatus.COMPLETED).method(PaymentMethod.PROVIDER)
                .providerReference("REF-002").build();

        WebhookPayloadRequest payload = WebhookPayloadRequest.builder()
                .providerReference("REF-002").provider("STRIPE").status("SUCCESS").build();

        when(paymentRepository.findByProviderReference("REF-002")).thenReturn(Optional.of(payment));

        paymentService.handleWebhook(payload);

        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(ticketService, bookingRepository);
    }

    @Test
    @DisplayName("handleWebhook — FAILED status sets payment to FAILED, no booking confirmation")
    void handleWebhook_failed() {
        Payment payment = Payment.builder().booking(booking).tenantId(tenantId)
                .amount(BigDecimal.valueOf(200)).currency("NGN")
                .status(PaymentStatus.PENDING).method(PaymentMethod.PROVIDER)
                .providerReference("REF-003").build();

        WebhookPayloadRequest payload = WebhookPayloadRequest.builder()
                .providerReference("REF-003").provider("STRIPE").status("FAILED").build();

        when(paymentRepository.findByProviderReference("REF-003")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);

        paymentService.handleWebhook(payload);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verifyNoInteractions(ticketService, bookingRepository);
    }
}
