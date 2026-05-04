package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.TenantConfig;
import com.emekachukwulobe.flightbookingservice.repository.TenantConfigRepository;
import com.emekachukwulobe.flightbookingservice.repository.TenantRepository;
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
    private static final String EMAIL_ENABLED            = "EMAIL_ENABLED";
    private static final String SMS_ENABLED              = "SMS_ENABLED";

    private final TenantConfigRepository tenantConfigRepository;
    private final TenantRepository tenantRepository;

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
            .map(TenantConfig::getValue)
            .orElse(defaultValue);
    }

    @Override
    @Transactional
    public void upsertConfig(UUID tenantId, String name, String value) {
        TenantConfig config = tenantConfigRepository.findByTenantIdAndName(tenantId, name)
            .orElseGet(() -> {
                var tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
                return TenantConfig.builder()
                    .tenant(tenant)
                    .name(name)
                    .value("")
                    .build();
            });
        config.setValue(value);
        tenantConfigRepository.save(config);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailEnabled(UUID tenantId) {
        return Boolean.parseBoolean(getConfigValue(tenantId, EMAIL_ENABLED, "false"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSmsEnabled(UUID tenantId) {
        return Boolean.parseBoolean(getConfigValue(tenantId, SMS_ENABLED, "false"));
    }
}
