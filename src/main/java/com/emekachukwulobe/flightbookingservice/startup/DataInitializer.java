package com.emekachukwulobe.flightbookingservice.startup;

import com.emekachukwulobe.flightbookingservice.config.AppProperties;
import com.emekachukwulobe.flightbookingservice.domain.PasswordHistory;
import com.emekachukwulobe.flightbookingservice.domain.User;
import com.emekachukwulobe.flightbookingservice.domain.enums.UserRole;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateTenantRequest;
import com.emekachukwulobe.flightbookingservice.repository.PasswordHistoryRepository;
import com.emekachukwulobe.flightbookingservice.repository.TenantRepository;
import com.emekachukwulobe.flightbookingservice.repository.UserRepository;
import com.emekachukwulobe.flightbookingservice.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final TenantRepository tenantRepository;
    private final TenantService tenantService;
    private final UserRepository userRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var config = appProperties.getDataInitializer();

        String tenantCode    = config.getDefaultTenantCode();
        String tenantName    = config.getDefaultTenantName();
        String adminUsername = config.getDefaultAdminUsername();
        String adminPassword = config.getDefaultAdminPassword();

        if (tenantCode == null || adminUsername == null) {
            log.info("DataInitializer: no default tenant configured — skipping bootstrap");
            return;
        }

        if (tenantRepository.existsByCode(tenantCode)) {
            log.info("DataInitializer: default tenant '{}' already exists — skipping", tenantCode);
            return;
        }

        log.info("DataInitializer: bootstrapping default tenant '{}'", tenantCode);

        // Create the default tenant (seeds TenantConfig rows automatically)
        var tenantResponse = tenantService.createTenant(
            CreateTenantRequest.builder().name(tenantName).code(tenantCode).build());

        var tenant = tenantService.getEntityById(tenantResponse.getId());

        // Create the default admin user
        String encodedPassword = passwordEncoder.encode(adminPassword);
        User admin = User.builder()
            .tenant(tenant)
            .username(adminUsername)
            .passwordHash(encodedPassword)
            .role(UserRole.ADMIN)
            .active(true)
            .build();
        User savedAdmin = userRepository.save(admin);

        passwordHistoryRepository.save(
            PasswordHistory.builder()
                .user(savedAdmin)
                .tenantId(tenant.getId())
                .passwordHash(encodedPassword)
                .build()
        );

        log.info("DataInitializer: default admin user '{}' created for tenant '{}'", adminUsername, tenantCode);
    }
}
