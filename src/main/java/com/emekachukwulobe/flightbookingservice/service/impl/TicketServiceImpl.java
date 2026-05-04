package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Booking;
import com.emekachukwulobe.flightbookingservice.domain.Passenger;
import com.emekachukwulobe.flightbookingservice.domain.Seat;
import com.emekachukwulobe.flightbookingservice.domain.Ticket;
import com.emekachukwulobe.flightbookingservice.domain.enums.SeatStatus;
import com.emekachukwulobe.flightbookingservice.domain.enums.TicketStatus;
import com.emekachukwulobe.flightbookingservice.dto.response.TicketResponse;
import com.emekachukwulobe.flightbookingservice.mapper.TicketMapper;
import com.emekachukwulobe.flightbookingservice.repository.SeatRepository;
import com.emekachukwulobe.flightbookingservice.repository.TicketRepository;
import com.emekachukwulobe.flightbookingservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public List<TicketResponse> issueTickets(Booking booking) {
        List<Passenger> passengers = booking.getPassengers();
        List<Seat> reservedSeats = booking.getReservedSeats();

        List<Ticket> tickets = new ArrayList<>();
        List<Seat> seatsToOccupy = new ArrayList<>();

        for (int i = 0; i < passengers.size(); i++) {
            Passenger passenger = passengers.get(i);
            Seat seat = (reservedSeats != null && i < reservedSeats.size()) ? reservedSeats.get(i) : null;

            if (seat != null) {
                seat.setStatus(SeatStatus.OCCUPIED);
                seatsToOccupy.add(seat);
            }

            tickets.add(Ticket.builder()
                .booking(booking)
                .passenger(passenger)
                .fare(booking.getFlight().getFares().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No fares configured for flight: " + booking.getFlight().getId())))
                .seat(seat)
                .seatNumber(seat != null ? seat.getSeatNumber() : null)
                .tenantId(booking.getTenantId())
                .ticketNumber(generateTicketNumber())
                .status(TicketStatus.ISSUED)
                .issuedAt(OffsetDateTime.now())
                .build());
        }

        if (!seatsToOccupy.isEmpty()) {
            seatRepository.saveAll(seatsToOccupy);
        }

        List<Ticket> saved = ticketRepository.saveAll(tickets);
        return ticketMapper.toResponseList(saved);
    }

    @Override
    @Transactional
    public void voidTickets(Booking booking) {
        List<Ticket> tickets = ticketRepository.findAllByBookingId(booking.getId());

        // Release seat statuses
        List<Seat> seatsToRelease = tickets.stream()
            .filter(t -> t.getSeat() != null)
            .map(Ticket::getSeat)
            .toList();

        seatsToRelease.forEach(s -> s.setStatus(SeatStatus.AVAILABLE));
        if (!seatsToRelease.isEmpty()) {
            seatRepository.saveAll(seatsToRelease);
        }

        tickets.forEach(t -> t.setStatus(TicketStatus.VOID));
        ticketRepository.saveAll(tickets);
    }

    private String generateTicketNumber() {
        return "TKT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
