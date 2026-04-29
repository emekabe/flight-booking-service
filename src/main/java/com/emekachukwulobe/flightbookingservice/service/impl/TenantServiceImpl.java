package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.Tenant;
import com.emekachukwulobe.flightbookingservice.domain.TenantConfig;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateTenantRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.TenantResponse;
import com.emekachukwulobe.flightbookingservice.exception.DuplicateResourceException;
import com.emekachukwulobe.flightbookingservice.exception.TenantNotFoundException;
import com.emekachukwulobe.flightbookingservice.mapper.TenantMapper;
import com.emekachukwulobe.flightbookingservice.repository.TenantRepository;
import com.emekachukwulobe.flightbookingservice.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;

    @Override
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        if (tenantRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Tenant with code already exists: " + request.getCode());
        }

        Tenant tenant = Tenant.builder()
            .name(request.getName())
            .code(request.getCode().toUpperCase())
            .active(true)
            .build();

        seedDefaultConfigs(tenant);
        Tenant saved = tenantRepository.save(tenant);
        return tenantMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Tenant getEntityByCode(String code) {
        return tenantRepository.findByCode(code)
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public Tenant getEntityById(UUID tenantId) {
        return tenantRepository.findByIdAndActiveTrue(tenantId)
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));
    }

    private void seedDefaultConfigs(Tenant tenant) {
        List<TenantConfig> defaults = List.of(
            TenantConfig.builder().tenant(tenant).name("password_history_enabled").value("false").build(),
            TenantConfig.builder().tenant(tenant).name("password_history_count").value("5").build()
        );
        tenant.getConfigs().addAll(defaults);
    }
}
