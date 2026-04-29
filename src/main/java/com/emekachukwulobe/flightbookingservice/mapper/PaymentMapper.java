package com.emekachukwulobe.flightbookingservice.mapper;

import com.emekachukwulobe.flightbookingservice.domain.Payment;
import com.emekachukwulobe.flightbookingservice.dto.response.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "booking.bookingReference", target = "bookingReference")
    PaymentResponse toResponse(Payment payment);
}
