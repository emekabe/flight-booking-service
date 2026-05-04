package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.config.AppProperties;
import com.emekachukwulobe.flightbookingservice.domain.*;
import com.emekachukwulobe.flightbookingservice.domain.enums.*;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateBookingRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.PassengerRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.BookingResponse;
import com.emekachukwulobe.flightbookingservice.exception.*;
import com.emekachukwulobe.flightbookingservice.mapper.BookingMapper;
import com.emekachukwulobe.flightbookingservice.repository.*;
import com.emekachukwulobe.flightbookingservice.service.NotificationService;
import com.emekachukwulobe.flightbookingservice.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl unit tests")
class BookingServiceImplTest {

    @Mock BookingRepository         bookingRepository;
    @Mock FlightRepository          flightRepository;
    @Mock FlightInventoryRepository inventoryRepository;
    @Mock FareRepository            fareRepository;
    @Mock SeatRepository            seatRepository;
    @Mock UserRepository            userRepository;
    @Mock TicketService             ticketService;
    @Mock NotificationService       notificationService;
    @Mock BookingMapper             bookingMapper;
    @Mock AppProperties             appProperties;

    @InjectMocks BookingServiceImpl bookingService;

    UUID tenantId, userId, flightId, fareId;
    Flight  flight;
    Fare    fare;
    User    user;
    FlightInventory inventory;
    AppProperties.Booking bookingConfig;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId   = UUID.randomUUID();
        flightId = UUID.randomUUID();
        fareId   = UUID.randomUUID();

        Tenant tenant = Tenant.builder().name("Air Test").code("AT").build();
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        fare = Fare.builder().tenantId(tenantId).fareClass(FareClass.ECONOMY)
                .price(BigDecimal.valueOf(150)).currency("USD").build();
        ReflectionTestUtils.setField(fare, "id", fareId);

        flight = Flight.builder().tenant(tenant).flightNumber("AT001")
                .origin("LOS").destination("ABV")
                .departureTime(OffsetDateTime.now().plusDays(1))
                .arrivalTime(OffsetDateTime.now().plusDays(1).plusHours(1))
                .status(FlightStatus.SCHEDULED).build();
        ReflectionTestUtils.setField(flight, "id", flightId);
        flight.getFares().add(fare);
        fare = Fare.builder().flight(flight).tenantId(tenantId)
                .fareClass(FareClass.ECONOMY).price(BigDecimal.valueOf(150)).currency("USD").build();
        ReflectionTestUtils.setField(fare, "id", fareId);

        inventory = FlightInventory.builder().flight(flight).tenantId(tenantId)
                .totalSeats(100).availableSeats(50).build();
        ReflectionTestUtils.setField(inventory, "version", 0L);

        user = User.builder().tenant(tenant).username("agent1")
                .passwordHash("hash").role(UserRole.AGENT).build();
        ReflectionTestUtils.setField(user, "id", userId);

        bookingConfig = new AppProperties.Booking();
        bookingConfig.setExpiryMinutes(30);
    }

    private CreateBookingRequest validRequest() {
        return CreateBookingRequest.builder()
                .flightId(flightId).fareId(fareId)
                .passengers(List.of(PassengerRequest.builder()
                        .firstName("John").lastName("Doe").build()))
                .build();
    }

    @Test
    @DisplayName("createBooking — success: seats reserved and booking persisted")
    void createBooking_success() {
        when(flightRepository.findByIdAndTenantId(flightId, tenantId)).thenReturn(Optional.of(flight));
        when(fareRepository.findByIdAndTenantId(fareId, tenantId)).thenReturn(Optional.of(fare));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(inventoryRepository.findByFlightIdAndTenantIdWithLock(flightId, tenantId))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any())).thenReturn(inventory);
        when(appProperties.getBooking()).thenReturn(bookingConfig);
        // No aircraft assigned → seat map is empty → auto-assignment skipped
        when(seatRepository.findAvailableByFlightIdAndCabinClass(eq(flightId), eq(FareClass.ECONOMY)))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            ReflectionTestUtils.setField(b, "id", UUID.randomUUID());
            return b;
        });
        BookingResponse expected = BookingResponse.builder().bookingReference("ABC123")
                .status(BookingStatus.PENDING).build();
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(expected);
        // Notification is async — no stub needed, but verify it's called
        doNothing().when(notificationService).sendBookingConfirmation(any());

        BookingResponse result = bookingService.createBooking(validRequest(), tenantId, userId);

        assertThat(result.getBookingReference()).isEqualTo("ABC123");
        assertThat(inventory.getAvailableSeats()).isEqualTo(49); // 50 - 1
        verify(inventoryRepository).save(inventory);
        verify(bookingRepository).save(any(Booking.class));
        verify(notificationService).sendBookingConfirmation(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking — fails when flight not found")
    void createBooking_flightNotFound() {
        when(flightRepository.findByIdAndTenantId(flightId, tenantId)).thenReturn(Optional.empty());
        assertThrows(FlightNotFoundException.class,
                () -> bookingService.createBooking(validRequest(), tenantId, userId));
    }

    @Test
    @DisplayName("createBooking — throws InsufficientSeatsException when no seats")
    void createBooking_insufficientSeats() {
        inventory.setAvailableSeats(0);
        when(flightRepository.findByIdAndTenantId(flightId, tenantId)).thenReturn(Optional.of(flight));
        when(fareRepository.findByIdAndTenantId(fareId, tenantId)).thenReturn(Optional.of(fare));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(inventoryRepository.findByFlightIdAndTenantIdWithLock(flightId, tenantId))
                .thenReturn(Optional.of(inventory));

        assertThrows(InsufficientSeatsException.class,
                () -> bookingService.createBooking(validRequest(), tenantId, userId));
    }

    @Test
    @DisplayName("createBooking — optimistic lock translates to InsufficientSeatsException")
    void createBooking_optimisticLockConflict() {
        when(flightRepository.findByIdAndTenantId(flightId, tenantId)).thenReturn(Optional.of(flight));
        when(fareRepository.findByIdAndTenantId(fareId, tenantId)).thenReturn(Optional.of(fare));
        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(inventoryRepository.findByFlightIdAndTenantIdWithLock(flightId, tenantId))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(FlightInventory.class, inventory));

        assertThrows(InsufficientSeatsException.class,
                () -> bookingService.createBooking(validRequest(), tenantId, userId));
    }

    @Test
    @DisplayName("confirmBooking — transitions PENDING to CONFIRMED and issues tickets")
    void confirmBooking_success() {
        String ref = "PNR001";
        Booking booking = Booking.builder().tenantId(tenantId).bookingReference(ref)
                .flight(flight).user(user).status(BookingStatus.PENDING).build();
        ReflectionTestUtils.setField(booking, "id", UUID.randomUUID());
        BookingResponse expected = BookingResponse.builder().bookingReference(ref)
                .status(BookingStatus.CONFIRMED).build();

        when(bookingRepository.findByBookingReferenceAndTenantId(ref, tenantId))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponse(any())).thenReturn(expected);
        doNothing().when(notificationService).sendTicketDetails(any());

        BookingResponse result = bookingService.confirmBooking(ref, tenantId);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(ticketService).issueTickets(booking);
        verify(notificationService).sendTicketDetails(any(Booking.class));
    }

    @Test
    @DisplayName("confirmBooking — throws BookingException when already cancelled")
    void confirmBooking_alreadyCancelled() {
        String ref = "PNR002";
        Booking booking = Booking.builder().tenantId(tenantId).bookingReference(ref)
                .flight(flight).user(user).status(BookingStatus.CANCELLED).build();

        when(bookingRepository.findByBookingReferenceAndTenantId(ref, tenantId))
                .thenReturn(Optional.of(booking));

        assertThrows(BookingException.class, () -> bookingService.confirmBooking(ref, tenantId));
        verifyNoInteractions(ticketService);
    }

    @Test
    @DisplayName("cancelBooking — releases seats and voids tickets")
    void cancelBooking_success() {
        String ref = "PNR003";
        Booking booking = Booking.builder().tenantId(tenantId).bookingReference(ref)
                .flight(flight).user(user).status(BookingStatus.PENDING).build();
        ReflectionTestUtils.setField(booking, "id", UUID.randomUUID());
        booking.getPassengers().add(Passenger.builder().booking(booking).tenantId(tenantId)
                .firstName("Jane").lastName("Doe").build());

        when(bookingRepository.findByBookingReferenceAndTenantId(ref, tenantId))
                .thenReturn(Optional.of(booking));
        // No seat entities for this flight → seat release is skipped
        when(seatRepository.findAllByFlightId(flightId)).thenReturn(List.of());
        when(inventoryRepository.findByFlightId(flightId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any())).thenReturn(inventory);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponse(any())).thenReturn(
                BookingResponse.builder().bookingReference(ref).status(BookingStatus.CANCELLED).build());

        bookingService.cancelBooking(ref, tenantId);

        assertThat(inventory.getAvailableSeats()).isEqualTo(51); // released 1 seat
        verify(ticketService).voidTickets(booking);
        verify(bookingRepository).save(argThat(b -> b.getStatus() == BookingStatus.CANCELLED));
    }
}
