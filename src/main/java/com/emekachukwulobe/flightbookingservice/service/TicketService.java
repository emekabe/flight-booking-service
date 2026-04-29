package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.domain.Booking;
import com.emekachukwulobe.flightbookingservice.dto.response.TicketResponse;

import java.util.List;

public interface TicketService {

    List<TicketResponse> issueTickets(Booking booking);

    void voidTickets(Booking booking);
}
