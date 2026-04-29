package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.dto.request.CreateBookingRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.BookingResponse;

import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request, UUID tenantId, UUID userId);

    BookingResponse getBooking(String bookingReference, UUID tenantId);

    BookingResponse confirmBooking(String bookingReference, UUID tenantId);

    BookingResponse cancelBooking(String bookingReference, UUID tenantId);

    void expireStaleBookings();
}
