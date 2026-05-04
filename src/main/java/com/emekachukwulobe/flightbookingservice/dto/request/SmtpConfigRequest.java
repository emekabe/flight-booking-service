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
public class SmtpConfigRequest {

    @NotBlank
    private String host;

    @NotBlank
    private String port;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String fromEmail;

    @NotBlank
    private String fromName;
}
