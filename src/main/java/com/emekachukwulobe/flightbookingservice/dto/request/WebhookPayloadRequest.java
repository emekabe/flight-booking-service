package com.emekachukwulobe.flightbookingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayloadRequest {

    @NotBlank
    private String providerReference;

    @NotBlank
    private String provider;

    @NotBlank
    private String status;

    /** Arbitrary provider-specific metadata. */
    private Map<String, String> metadata;
}
