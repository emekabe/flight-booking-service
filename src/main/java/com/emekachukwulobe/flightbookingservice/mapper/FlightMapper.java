package com.emekachukwulobe.flightbookingservice.mapper;

import com.emekachukwulobe.flightbookingservice.domain.Fare;
import com.emekachukwulobe.flightbookingservice.domain.Flight;
import com.emekachukwulobe.flightbookingservice.dto.response.FareResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.FlightResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    @Mapping(source = "inventory.totalSeats",     target = "totalSeats")
    @Mapping(source = "inventory.availableSeats", target = "availableSeats")
    FlightResponse toResponse(Flight flight);

    List<FlightResponse> toResponseList(List<Flight> flights);

    FareResponse toFareResponse(Fare fare);
}
