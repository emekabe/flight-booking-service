package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.repository.TenantConfigRepository;
import com.emekachukwulobe.flightbookingservice.service.TenantConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantConfigServiceImpl implements TenantConfigService {

    private static final String PASSWORD_HISTORY_ENABLED = "password_history_enabled";
    private static final String PASSWORD_HISTORY_COUNT   = "password_history_count";

    private final TenantConfigRepository tenantConfigRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isPasswordHistoryEnabled(UUID tenantId) {
        return Boolean.parseBoolean(getConfigValue(tenantId, PASSWORD_HISTORY_ENABLED, "false"));
    }

    @Override
    @Transactional(readOnly = true)
    public int getPasswordHistoryCount(UUID tenantId) {
        return Integer.parseInt(getConfigValue(tenantId, PASSWORD_HISTORY_COUNT, "5"));
    }

    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(UUID tenantId, String name, String defaultValue) {
        return tenantConfigRepository.findByTenantIdAndName(tenantId, name)
            .map(config -> config.getValue())
            .orElse(defaultValue);
    }
}
