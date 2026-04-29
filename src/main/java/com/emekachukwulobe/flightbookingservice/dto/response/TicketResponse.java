package com.emekachukwulobe.flightbookingservice.dto.response;

import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import com.emekachukwulobe.flightbookingservice.domain.enums.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class TicketResponse {

    private UUID id;
    private String ticketNumber;
    private TicketStatus status;
    private FareClass fareClass;
    private String passengerName;
    private OffsetDateTime issuedAt;
}
