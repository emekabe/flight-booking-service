package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Tenant;
import com.emekachukwulobe.flightbookingservice.domain.User;
import com.emekachukwulobe.flightbookingservice.domain.enums.UserRole;
import com.emekachukwulobe.flightbookingservice.dto.response.LoginResponse;
import com.emekachukwulobe.flightbookingservice.security.TenantAwareUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceImplTest {

    private final AuthServiceImpl authService = new AuthServiceImpl();

    @Test
    @DisplayName("getLoginResponse extracts tenant code and user details successfully")
    void testGetLoginResponse() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Tenant tenant = Tenant.builder()
                .name("Delta Air")
                .code("DAL")
                .build();
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        User user = User.builder()
                .username("admin")
                .role(UserRole.ADMIN)
                .tenant(tenant)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        TenantAwareUserDetails userDetails = new TenantAwareUserDetails(user);

        LoginResponse response = authService.getLoginResponse(userDetails);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getTenantId()).isEqualTo(tenantId);
        assertThat(response.getTenantCode()).isEqualTo("DAL");
    }
}
