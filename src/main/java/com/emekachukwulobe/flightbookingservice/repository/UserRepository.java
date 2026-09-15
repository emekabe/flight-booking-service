package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = {"tenant"})
    Optional<User> findByUsername(String username);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByUsernameAndTenantId(String username, UUID tenantId);
}
