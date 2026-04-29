package com.emekachukwulobe.flightbookingservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class TenantResponse {

    private UUID id;
    private String name;
    private String code;
    private boolean active;
    private OffsetDateTime createdAt;
}
