package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.domain.Booking;

public interface NotificationService {

    /**
     * Sends a booking confirmation notification to all passengers.
     * Called after a booking is created (PENDING state).
     */
    void sendBookingConfirmation(Booking booking);

    /**
     * Sends ticket details to all passengers after booking is confirmed.
     * Includes seat number, ticket number, and flight info for airport presentation.
     */
    void sendTicketDetails(Booking booking);
}
