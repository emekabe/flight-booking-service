package com.emekachukwulobe.flightbookingservice.domain;

import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "fares")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fare extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fare_class", nullable = false, length = 20)
    private FareClass fareClass;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /** ISO 4217 currency code. */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
}
