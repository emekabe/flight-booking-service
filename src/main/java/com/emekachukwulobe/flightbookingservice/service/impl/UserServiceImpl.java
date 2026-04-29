package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.PasswordHistory;
import com.emekachukwulobe.flightbookingservice.domain.User;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateUserRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.UpdatePasswordRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.UserResponse;
import com.emekachukwulobe.flightbookingservice.exception.DuplicateResourceException;
import com.emekachukwulobe.flightbookingservice.exception.PasswordReuseException;
import com.emekachukwulobe.flightbookingservice.exception.UserNotFoundException;
import com.emekachukwulobe.flightbookingservice.mapper.UserMapper;
import com.emekachukwulobe.flightbookingservice.repository.PasswordHistoryRepository;
import com.emekachukwulobe.flightbookingservice.repository.UserRepository;
import com.emekachukwulobe.flightbookingservice.service.TenantConfigService;
import com.emekachukwulobe.flightbookingservice.service.TenantService;
import com.emekachukwulobe.flightbookingservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final TenantService tenantService;
    private final TenantConfigService tenantConfigService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request, UUID tenantId) {
        if (userRepository.existsByUsernameAndTenantId(request.getUsername(), tenantId)) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        var tenant = tenantService.getEntityById(tenantId);
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
            .tenant(tenant)
            .username(request.getUsername())
            .passwordHash(encodedPassword)
            .role(request.getRole())
            .active(true)
            .build();

        User saved = userRepository.save(user);

        // Seed initial password history entry
        passwordHistoryRepository.save(
            PasswordHistory.builder()
                .user(saved)
                .tenantId(tenantId)
                .passwordHash(encodedPassword)
                .build()
        );

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void updatePassword(UUID userId, UpdatePasswordRequest request, UUID tenantId) {
        User user = userRepository.findByIdAndTenantId(userId, tenantId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        enforcePasswordHistory(user, tenantId, request.getNewPassword());

        String encodedNew = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(encodedNew);
        userRepository.save(user);

        // Persist history entry and prune old ones
        passwordHistoryRepository.save(
            PasswordHistory.builder()
                .user(user)
                .tenantId(tenantId)
                .passwordHash(encodedNew)
                .build()
        );

        if (tenantConfigService.isPasswordHistoryEnabled(tenantId)) {
            int keepCount = tenantConfigService.getPasswordHistoryCount(tenantId);
            passwordHistoryRepository.deleteOldEntriesBeyondLimit(userId, keepCount);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId, UUID tenantId) {
        return userRepository.findByIdAndTenantId(userId, tenantId)
            .map(userMapper::toResponse)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    private void enforcePasswordHistory(User user, UUID tenantId, String newPassword) {
        if (!tenantConfigService.isPasswordHistoryEnabled(tenantId)) return;

        int historyCount = tenantConfigService.getPasswordHistoryCount(tenantId);
        List<PasswordHistory> recent = passwordHistoryRepository
            .findRecentByUserId(user.getId(), PageRequest.of(0, historyCount));

        boolean reused = recent.stream()
            .anyMatch(h -> passwordEncoder.matches(newPassword, h.getPasswordHash()));

        if (reused) {
            throw new PasswordReuseException(
                "Password was used recently. Please choose a different password (last " + historyCount + " not allowed).");
        }
    }
}
