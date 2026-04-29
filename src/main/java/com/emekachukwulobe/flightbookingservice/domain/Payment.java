package com.emekachukwulobe.flightbookingservice.domain;

import com.emekachukwulobe.flightbookingservice.domain.enums.PaymentMethod;
import com.emekachukwulobe.flightbookingservice.domain.enums.PaymentStatus;
import com.emekachukwulobe.flightbookingservice.domain.enums.ProviderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** ISO 4217 currency code. */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private PaymentMethod method;

    /**
     * Unique reference from the payment provider. Acts as the idempotency key
     * for webhook processing — the DB unique constraint prevents double-processing.
     */
    @Column(name = "provider_reference", unique = true, length = 255)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 50)
    private ProviderType provider;

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }
}
