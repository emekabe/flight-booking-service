package com.emekachukwulobe.flightbookingservice.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Utility class for extracting the authenticated principal from the SecurityContext.
 * Always call after {@link TenantResolutionFilter} has run (i.e., from a controller or service).
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static TenantAwareUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof TenantAwareUserDetails userDetails) {
            return userDetails;
        }
        throw new AccessDeniedException("No authenticated user in context");
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public static UUID getCurrentTenantId() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new AccessDeniedException("Tenant context not resolved");
        }
        return tenantId;
    }
}
