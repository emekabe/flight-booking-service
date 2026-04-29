package com.emekachukwulobe.flightbookingservice.security;

import com.emekachukwulobe.flightbookingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by username and wraps it in {@link TenantAwareUserDetails}.
     * The tenant association is fetched eagerly here to avoid a lazy-load exception
     * when {@link TenantResolutionFilter} reads the tenant ID outside a transaction.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .filter(user -> user.isActive())
            .map(user -> {
                // Eagerly initialize the tenant proxy while still inside the transaction
                user.getTenant().getId();
                return new TenantAwareUserDetails(user);
            })
            .orElseThrow(() -> new UsernameNotFoundException("User not found or inactive: " + username));
    }
}
