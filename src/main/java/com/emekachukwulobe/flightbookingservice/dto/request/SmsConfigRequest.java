package com.emekachukwulobe.flightbookingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsConfigRequest {

    @NotBlank
    private String accountSid;

    @NotBlank
    private String authToken;

    /** E.164 Twilio sender number, e.g. +12345678900 */
    @NotBlank
    private String fromNumber;
}
