package com.emekachukwulobe.flightbookingservice.security;

import java.util.UUID;

/**
 * Thread-local store for the current tenant ID, populated during request authentication
 * and cleared in the finally block of {@link TenantResolutionFilter}.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CONTEXT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentTenant(UUID tenantId) {
        CONTEXT.set(tenantId);
    }

    public static UUID getCurrentTenant() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
