package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Booking;
import com.emekachukwulobe.flightbookingservice.domain.Passenger;
import com.emekachukwulobe.flightbookingservice.domain.Ticket;
import com.emekachukwulobe.flightbookingservice.domain.enums.TicketStatus;
import com.emekachukwulobe.flightbookingservice.dto.response.TicketResponse;
import com.emekachukwulobe.flightbookingservice.mapper.TicketMapper;
import com.emekachukwulobe.flightbookingservice.repository.TicketRepository;
import com.emekachukwulobe.flightbookingservice.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public List<TicketResponse> issueTickets(Booking booking) {
        List<Ticket> tickets = booking.getPassengers().stream()
            .map(passenger -> buildTicket(booking, passenger))
            .toList();

        List<Ticket> saved = ticketRepository.saveAll(tickets);
        return ticketMapper.toResponseList(saved);
    }

    @Override
    @Transactional
    public void voidTickets(Booking booking) {
        List<Ticket> tickets = ticketRepository.findAllByBookingId(booking.getId());
        tickets.forEach(t -> t.setStatus(TicketStatus.VOID));
        ticketRepository.saveAll(tickets);
    }

    private Ticket buildTicket(Booking booking, Passenger passenger) {
        return Ticket.builder()
            .booking(booking)
            .passenger(passenger)
            .fare(booking.getFlight().getFares().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fares configured for flight: " + booking.getFlight().getId())))
            .tenantId(booking.getTenantId())
            .ticketNumber(generateTicketNumber())
            .status(TicketStatus.ISSUED)
            .issuedAt(OffsetDateTime.now())
            .build();
    }

    private String generateTicketNumber() {
        return "TKT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
