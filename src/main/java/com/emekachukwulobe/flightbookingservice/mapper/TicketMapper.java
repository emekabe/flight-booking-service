package com.emekachukwulobe.flightbookingservice.mapper;

import com.emekachukwulobe.flightbookingservice.domain.Ticket;
import com.emekachukwulobe.flightbookingservice.dto.response.TicketResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "fare.fareClass", target = "fareClass")
    @Mapping(
        expression = "java(ticket.getPassenger().getFirstName() + \" \" + ticket.getPassenger().getLastName())",
        target = "passengerName"
    )
    TicketResponse toResponse(Ticket ticket);

    List<TicketResponse> toResponseList(List<Ticket> tickets);
}
