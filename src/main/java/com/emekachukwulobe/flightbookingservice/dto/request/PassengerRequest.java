package com.emekachukwulobe.flightbookingservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerRequest {

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String passportNumber;

    private LocalDate dateOfBirth;

    /** Optional E.164 phone number for SMS notifications, e.g. +2348012345678. */
    @Size(max = 20)
    private String phoneNumber;

    /** Optional: preferred seat number (e.g. "12A" or "5"). Auto-assigned if omitted. */
    @Size(max = 10)
    private String preferredSeatNumber;
}

