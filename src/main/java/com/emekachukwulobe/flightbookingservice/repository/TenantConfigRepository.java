package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantConfigRepository extends JpaRepository<TenantConfig, UUID> {

    Optional<TenantConfig> findByTenantIdAndName(UUID tenantId, String name);

    List<TenantConfig> findAllByTenantId(UUID tenantId);
}
