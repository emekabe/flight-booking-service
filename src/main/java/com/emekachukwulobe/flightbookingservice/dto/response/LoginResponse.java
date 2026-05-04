package com.emekachukwulobe.flightbookingservice.dto.response;

import com.emekachukwulobe.flightbookingservice.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String username;
    private UserRole role;
    private UUID userId;
    private UUID tenantId;
    private String tenantCode;
}
