package com.emekachukwulobe.flightbookingservice.mapper;

import com.emekachukwulobe.flightbookingservice.domain.Booking;
import com.emekachukwulobe.flightbookingservice.domain.Passenger;
import com.emekachukwulobe.flightbookingservice.dto.response.BookingResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.PassengerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "flight.flightNumber",   target = "flightNumber")
    @Mapping(source = "flight.origin",         target = "origin")
    @Mapping(source = "flight.destination",    target = "destination")
    @Mapping(source = "flight.departureTime",  target = "departureTime")
    BookingResponse toResponse(Booking booking);

    PassengerResponse toPassengerResponse(Passenger passenger);
}
