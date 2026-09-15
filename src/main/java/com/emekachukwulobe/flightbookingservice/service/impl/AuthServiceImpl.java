package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.dto.response.LoginResponse;
import com.emekachukwulobe.flightbookingservice.security.TenantAwareUserDetails;
import com.emekachukwulobe.flightbookingservice.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    @Transactional(readOnly = true)
    public LoginResponse getLoginResponse(TenantAwareUserDetails userDetails) {
        return LoginResponse.builder()
                .username(userDetails.getUsername())
                .role(userDetails.getUser().getRole())
                .userId(userDetails.getUserId())
                .tenantId(userDetails.getTenantId())
                .tenantCode(userDetails.getTenantCode())
                .build();
    }
}
