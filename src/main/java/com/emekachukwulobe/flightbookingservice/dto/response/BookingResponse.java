package com.emekachukwulobe.flightbookingservice.dto.response;

import com.emekachukwulobe.flightbookingservice.domain.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

    private UUID id;
    private String bookingReference;
    private BookingStatus status;
    private String flightNumber;
    private String origin;
    private String destination;
    private OffsetDateTime departureTime;
    private OffsetDateTime expirationTime;
    private List<PassengerResponse> passengers;
    private List<TicketResponse> tickets;
    private PaymentResponse payment;
    private OffsetDateTime createdAt;
}
