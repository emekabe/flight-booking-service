package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.domain.Booking;
import com.emekachukwulobe.flightbookingservice.domain.Passenger;
import com.emekachukwulobe.flightbookingservice.domain.Ticket;

/**
 * Builds HTML email bodies for booking-related notifications.
 */
public interface EmailTemplateBuilder {

    /** Builds a booking confirmation email for a passenger. */
    String buildBookingConfirmation(Booking booking, Passenger passenger);

    /** Builds a ticket details email for a passenger with their ticket info. */
    String buildTicketDetails(Booking booking, Passenger passenger, Ticket ticket);
}
