package com.emekachukwulobe.flightbookingservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs after Spring Security's {@code BasicAuthenticationFilter} to populate
 * {@link TenantContext} from the authenticated principal.
 * Clears the context in a finally block to prevent thread-local leakage.
 */
@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof TenantAwareUserDetails userDetails) {
                TenantContext.setCurrentTenant(userDetails.getTenantId());
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
