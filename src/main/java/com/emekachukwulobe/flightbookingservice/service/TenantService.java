package com.emekachukwulobe.flightbookingservice.service;

import com.emekachukwulobe.flightbookingservice.domain.Tenant;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateTenantRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.TenantResponse;

import java.util.UUID;

public interface TenantService {

    TenantResponse createTenant(CreateTenantRequest request);

    Tenant getEntityByCode(String code);

    Tenant getEntityById(UUID tenantId);
}
