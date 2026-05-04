package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Booking;
import com.emekachukwulobe.flightbookingservice.domain.Fare;
import com.emekachukwulobe.flightbookingservice.domain.Passenger;
import com.emekachukwulobe.flightbookingservice.domain.Seat;
import com.emekachukwulobe.flightbookingservice.domain.User;
import com.emekachukwulobe.flightbookingservice.domain.enums.BookingStatus;
import com.emekachukwulobe.flightbookingservice.domain.enums.SeatStatus;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateBookingRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.PassengerRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.BookingResponse;
import com.emekachukwulobe.flightbookingservice.exception.BookingException;
import com.emekachukwulobe.flightbookingservice.exception.BookingNotFoundException;
import com.emekachukwulobe.flightbookingservice.exception.FlightNotFoundException;
import com.emekachukwulobe.flightbookingservice.exception.InsufficientSeatsException;
import com.emekachukwulobe.flightbookingservice.exception.UserNotFoundException;
import com.emekachukwulobe.flightbookingservice.mapper.BookingMapper;
import com.emekachukwulobe.flightbookingservice.repository.BookingRepository;
import com.emekachukwulobe.flightbookingservice.repository.FareRepository;
import com.emekachukwulobe.flightbookingservice.repository.FlightInventoryRepository;
import com.emekachukwulobe.flightbookingservice.repository.FlightRepository;
import com.emekachukwulobe.flightbookingservice.repository.SeatRepository;
import com.emekachukwulobe.flightbookingservice.repository.UserRepository;
import com.emekachukwulobe.flightbookingservice.service.BookingService;
import com.emekachukwulobe.flightbookingservice.service.NotificationService;
import com.emekachukwulobe.flightbookingservice.service.TicketService;
import com.emekachukwulobe.flightbookingservice.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final FlightInventoryRepository inventoryRepository;
    private final FareRepository fareRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;
    private final NotificationService notificationService;
    private final BookingMapper bookingMapper;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, UUID tenantId, UUID userId) {
        var flight = flightRepository.findByIdAndTenantId(request.getFlightId(), tenantId)
            .orElseThrow(() -> new FlightNotFoundException("Flight not found: " + request.getFlightId()));

        Fare fare = fareRepository.findByIdAndTenantId(request.getFareId(), tenantId)
            .orElseThrow(() -> new FlightNotFoundException("Fare not found: " + request.getFareId()));

        // Validate fare belongs to the requested flight
        if (!fare.getFlight().getId().equals(flight.getId())) {
            throw new BookingException("Fare " + request.getFareId() + " does not belong to flight " + request.getFlightId());
        }

        var user = userRepository.findByIdAndTenantId(userId, tenantId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        int seatCount = request.getPassengers().size();

        try {
            var inventory = inventoryRepository
                .findByFlightIdAndTenantIdWithLock(request.getFlightId(), tenantId)
                .orElseThrow(() -> new FlightNotFoundException("Inventory not found for flight: " + request.getFlightId()));

            inventory.reserveSeats(seatCount);
            inventoryRepository.save(inventory);

        } catch (IllegalStateException e) {
            throw new InsufficientSeatsException("Not enough seats available: requested " + seatCount);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new InsufficientSeatsException("Seat reservation conflict — please retry your booking");
        }

        Booking booking = Booking.builder()
            .tenantId(tenantId)
            .bookingReference(generateReference())
            .flight(flight)
            .user(user)
            .status(BookingStatus.PENDING)
            .expirationTime(OffsetDateTime.now().plusMinutes(appProperties.getBooking().getExpiryMinutes()))
            .build();

        // Assign seats per passenger
        List<Seat> reservedSeats = assignSeats(request, flight.getId(), tenantId, fare);

        List<Passenger> passengers = new ArrayList<>();
        List<PassengerRequest> passengerRequests = request.getPassengers();
        for (int i = 0; i < passengerRequests.size(); i++) {
            PassengerRequest p = passengerRequests.get(i);
            Seat assignedSeat = reservedSeats.isEmpty() ? null : reservedSeats.get(i);
            passengers.add(Passenger.builder()
                .booking(booking)
                .tenantId(tenantId)
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .email(p.getEmail())
                .passportNumber(p.getPassportNumber())
                .dateOfBirth(p.getDateOfBirth())
                .phoneNumber(p.getPhoneNumber())
                .build());
            if (assignedSeat != null) {
                assignedSeat.setStatus(SeatStatus.RESERVED);
            }
        }

        if (!reservedSeats.isEmpty()) {
            seatRepository.saveAll(reservedSeats);
        }

        booking.getPassengers().addAll(passengers);
        // Store seat references in transient list for ticket issuance later
        booking.setReservedSeats(reservedSeats);

        Booking saved = bookingRepository.save(booking);
        notificationService.sendBookingConfirmation(saved);
        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBooking(String bookingReference, UUID tenantId) {
        return bookingMapper.toResponse(findOrThrow(bookingReference, tenantId));
    }

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public BookingResponse confirmBooking(String bookingReference, UUID tenantId) {
        Booking booking = findOrThrow(bookingReference, tenantId);

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return bookingMapper.toResponse(booking);
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Cannot confirm a cancelled booking: " + bookingReference);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpirationTime(null);
        Booking saved = bookingRepository.save(booking);
        ticketService.issueTickets(saved);
        notificationService.sendTicketDetails(saved);

        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public BookingResponse cancelBooking(String bookingReference, UUID tenantId) {
        Booking booking = findOrThrow(bookingReference, tenantId);

        if (!booking.isCancellable()) {
            throw new BookingException("Booking cannot be cancelled: " + bookingReference);
        }

        releaseSeats(booking);
        ticketService.voidTickets(booking);

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public void expireStaleBookings() {
        List<Booking> expired = bookingRepository.findExpiredBookings(
            BookingStatus.PENDING, OffsetDateTime.now());

        if (expired.isEmpty()) return;

        log.info("Expiring {} stale bookings", expired.size());
        for (Booking booking : expired) {
            releaseSeats(booking);
            booking.setStatus(BookingStatus.CANCELLED);
        }
        bookingRepository.saveAll(expired);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Assigns seats to each passenger.
     * If a preferred seat is provided, validates it is AVAILABLE and matches the fare's cabin class.
     * Otherwise, auto-assigns a random available seat of the correct cabin class.
     * Returns the reserved Seat entities (not yet saved — callers must persist them).
     */
    private List<Seat> assignSeats(CreateBookingRequest request, UUID flightId, UUID tenantId, Fare fare) {
        // Seats may not exist if no aircraft was assigned
        List<Seat> availablePool = seatRepository.findAvailableByFlightIdAndCabinClass(flightId, fare.getFareClass());
        if (availablePool.isEmpty()) {
            // No seat map for this flight — seats not tracked individually
            return List.of();
        }

        List<Seat> assigned = new ArrayList<>();
        List<Seat> poolCopy = new ArrayList<>(availablePool);

        for (PassengerRequest p : request.getPassengers()) {
            if (p.getPreferredSeatNumber() != null && !p.getPreferredSeatNumber().isBlank()) {
                Seat preferred = seatRepository
                    .findByFlightIdAndSeatNumberWithLock(flightId, p.getPreferredSeatNumber())
                    .orElseThrow(() -> new BookingException("Seat not found: " + p.getPreferredSeatNumber()));

                if (preferred.getStatus() != SeatStatus.AVAILABLE) {
                    throw new BookingException("Seat " + p.getPreferredSeatNumber() + " is not available");
                }
                if (!preferred.getCabinClass().equals(fare.getFareClass())) {
                    throw new BookingException("Seat " + p.getPreferredSeatNumber()
                        + " is not in cabin class " + fare.getFareClass());
                }
                poolCopy.remove(preferred);
                assigned.add(preferred);
            } else {
                if (poolCopy.isEmpty()) {
                    throw new InsufficientSeatsException("Not enough seats in cabin class " + fare.getFareClass());
                }
                int idx = (int) (Math.random() * poolCopy.size());
                assigned.add(poolCopy.remove(idx));
            }
        }
        return assigned;
    }

    private void releaseSeats(Booking booking) {
        // Release seat entities if tracked
        List<Seat> seats = seatRepository.findAllByFlightId(booking.getFlight().getId())
            .stream()
            .filter(s -> s.getStatus() == SeatStatus.RESERVED || s.getStatus() == SeatStatus.OCCUPIED)
            .filter(s -> booking.getTickets().stream()
                .anyMatch(t -> t.getSeat() != null && t.getSeat().getId().equals(s.getId())))
            .toList();
        seats.forEach(s -> s.setStatus(SeatStatus.AVAILABLE));
        if (!seats.isEmpty()) seatRepository.saveAll(seats);

        // Always release inventory count
        inventoryRepository.findByFlightId(booking.getFlight().getId()).ifPresent(inv -> {
            inv.releaseSeats(booking.getPassengers().size());
            inventoryRepository.save(inv);
        });
    }

    private Booking findOrThrow(String reference, UUID tenantId) {
        return bookingRepository.findByBookingReferenceAndTenantId(reference, tenantId)
            .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + reference));
    }

    private String generateReference() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
}
