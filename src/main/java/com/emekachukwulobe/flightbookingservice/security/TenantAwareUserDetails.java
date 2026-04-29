package com.emekachukwulobe.flightbookingservice.security;

import com.emekachukwulobe.flightbookingservice.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Security {@link UserDetails} wrapper around the domain {@link User},
 * carrying the tenant ID for downstream {@link TenantContext} population.
 */
public class TenantAwareUserDetails implements UserDetails {

    private final User user;

    public TenantAwareUserDetails(User user) {
        this.user = user;
    }

    public UUID getTenantId() {
        return user.getTenant().getId();
    }

    public UUID getUserId() {
        return user.getId();
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
