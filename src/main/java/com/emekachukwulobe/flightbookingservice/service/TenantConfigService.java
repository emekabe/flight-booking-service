package com.emekachukwulobe.flightbookingservice.service;

import java.util.UUID;

public interface TenantConfigService {

    boolean isPasswordHistoryEnabled(UUID tenantId);

    int getPasswordHistoryCount(UUID tenantId);

    String getConfigValue(UUID tenantId, String name, String defaultValue);

    void upsertConfig(UUID tenantId, String name, String value);

    boolean isEmailEnabled(UUID tenantId);

    boolean isSmsEnabled(UUID tenantId);
}
