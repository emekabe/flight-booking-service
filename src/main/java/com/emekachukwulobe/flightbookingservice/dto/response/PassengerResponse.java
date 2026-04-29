package com.emekachukwulobe.flightbookingservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class PassengerResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String passportNumber;
    private LocalDate dateOfBirth;
}
