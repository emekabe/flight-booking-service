package com.emekachukwulobe.flightbookingservice.service.impl;

import com.emekachukwulobe.flightbookingservice.domain.*;
import com.emekachukwulobe.flightbookingservice.domain.enums.UserRole;
import com.emekachukwulobe.flightbookingservice.dto.request.CreateUserRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.UpdatePasswordRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.UserResponse;
import com.emekachukwulobe.flightbookingservice.exception.*;
import com.emekachukwulobe.flightbookingservice.mapper.UserMapper;
import com.emekachukwulobe.flightbookingservice.repository.PasswordHistoryRepository;
import com.emekachukwulobe.flightbookingservice.repository.UserRepository;
import com.emekachukwulobe.flightbookingservice.service.TenantConfigService;
import com.emekachukwulobe.flightbookingservice.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl unit tests — password history policy")
class UserServiceImplTest {

    @Mock UserRepository            userRepository;
    @Mock PasswordHistoryRepository passwordHistoryRepository;
    @Mock TenantService             tenantService;
    @Mock TenantConfigService       tenantConfigService;
    @Mock PasswordEncoder           passwordEncoder;
    @Mock UserMapper                userMapper;

    @InjectMocks UserServiceImpl userService;

    UUID tenantId, userId;
    Tenant tenant;
    User   user;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId   = UUID.randomUUID();

        tenant = Tenant.builder().name("Test Airline").code("TA").build();
        ReflectionTestUtils.setField(tenant, "id", tenantId);

        user = User.builder().tenant(tenant).username("agent@test.com")
                .passwordHash("$bcrypt$old_hash").role(UserRole.AGENT).build();
        ReflectionTestUtils.setField(user, "id", userId);
    }

    // ── createUser ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("createUser — success: user saved and initial password history seeded")
    void createUser_success() {
        CreateUserRequest req = CreateUserRequest.builder()
                .username("newagent").password("Secret1234!").role(UserRole.AGENT).build();

        when(userRepository.existsByUsernameAndTenantId("newagent", tenantId)).thenReturn(false);
        when(tenantService.getEntityById(tenantId)).thenReturn(tenant);
        when(passwordEncoder.encode("Secret1234!")).thenReturn("$bcrypt$new_hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
            return u;
        });
        when(passwordHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any())).thenReturn(
                UserResponse.builder().username("newagent").role(UserRole.AGENT).build());

        UserResponse result = userService.createUser(req, tenantId);

        assertThat(result.getUsername()).isEqualTo("newagent");
        verify(passwordHistoryRepository).save(argThat(h ->
                h.getPasswordHash().equals("$bcrypt$new_hash") && h.getTenantId().equals(tenantId)));
    }

    @Test
    @DisplayName("createUser — throws DuplicateResourceException for existing username")
    void createUser_duplicateUsername() {
        when(userRepository.existsByUsernameAndTenantId("agent@test.com", tenantId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                userService.createUser(CreateUserRequest.builder()
                        .username("agent@test.com").password("pass").role(UserRole.AGENT).build(),
                        tenantId));
        verifyNoInteractions(passwordEncoder, tenantService);
    }

    // ── updatePassword ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePassword — success when history is disabled")
    void updatePassword_success_historyDisabled() {
        UpdatePasswordRequest req = new UpdatePasswordRequest("OldPass1!", "NewPass2!");

        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", user.getPasswordHash())).thenReturn(true);
        when(tenantConfigService.isPasswordHistoryEnabled(tenantId)).thenReturn(false);
        when(passwordEncoder.encode("NewPass2!")).thenReturn("$bcrypt$new_hash2");
        when(userRepository.save(any())).thenReturn(user);
        when(passwordHistoryRepository.save(any())).thenReturn(null);

        userService.updatePassword(userId, req, tenantId);

        verify(userRepository).save(argThat(u -> u.getPasswordHash().equals("$bcrypt$new_hash2")));
        verify(passwordHistoryRepository).save(argThat(h -> h.getPasswordHash().equals("$bcrypt$new_hash2")));
        // No history pruning when disabled
        verify(passwordHistoryRepository, never()).deleteOldEntriesBeyondLimit(any(), anyInt());
    }

    @Test
    @DisplayName("updatePassword — throws PasswordReuseException when new password matches recent history")
    void updatePassword_historyEnabled_reuseDetected() {
        UpdatePasswordRequest req = new UpdatePasswordRequest("OldPass1!", "ReusedPass!");

        PasswordHistory hist = PasswordHistory.builder().user(user).tenantId(tenantId)
                .passwordHash("$bcrypt$reused").build();

        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", user.getPasswordHash())).thenReturn(true);
        when(tenantConfigService.isPasswordHistoryEnabled(tenantId)).thenReturn(true);
        when(tenantConfigService.getPasswordHistoryCount(tenantId)).thenReturn(5);
        when(passwordHistoryRepository.findRecentByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(List.of(hist));
        when(passwordEncoder.matches("ReusedPass!", "$bcrypt$reused")).thenReturn(true);

        assertThrows(PasswordReuseException.class,
                () -> userService.updatePassword(userId, req, tenantId));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePassword — prunes old history entries when history is enabled")
    void updatePassword_historyEnabled_prunesOldEntries() {
        UpdatePasswordRequest req = new UpdatePasswordRequest("OldPass1!", "FreshPass99!");

        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1!", user.getPasswordHash())).thenReturn(true);
        when(tenantConfigService.isPasswordHistoryEnabled(tenantId)).thenReturn(true);
        when(tenantConfigService.getPasswordHistoryCount(tenantId)).thenReturn(5);
        when(passwordHistoryRepository.findRecentByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(List.of()); // empty history → no matches call
        when(passwordEncoder.encode("FreshPass99!")).thenReturn("$bcrypt$fresh");
        when(userRepository.save(any())).thenReturn(user);
        when(passwordHistoryRepository.save(any())).thenReturn(null);

        userService.updatePassword(userId, req, tenantId);

        verify(passwordHistoryRepository).deleteOldEntriesBeyondLimit(userId, 5);
    }

    @Test
    @DisplayName("updatePassword — throws BadCredentialsException for wrong current password")
    void updatePassword_wrongCurrentPassword() {
        UpdatePasswordRequest req = new UpdatePasswordRequest("WrongPass!", "NewPass2!");

        when(userRepository.findByIdAndTenantId(userId, tenantId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass!", user.getPasswordHash())).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> userService.updatePassword(userId, req, tenantId));
        verify(userRepository, never()).save(any());
    }
}
