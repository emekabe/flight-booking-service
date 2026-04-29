package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByBookingBookingReferenceAndTenantId(String bookingReference, UUID tenantId);

    /**
     * Used for webhook idempotency checking before processing a provider callback.
     */
    Optional<Payment> findByProviderReference(String providerReference);

    boolean existsByProviderReference(String providerReference);
}
