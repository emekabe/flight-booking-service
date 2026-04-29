package com.emekachukwulobe.flightbookingservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "flight_inventories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInventory extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false, unique = true)
    private Flight flight;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    /**
     * Hibernate optimistic lock version. Prevents concurrent seat over-booking
     * without requiring a pessimistic database-level lock.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public void reserveSeats(int count) {
        if (availableSeats < count) {
            throw new IllegalStateException("Insufficient seats available");
        }
        this.availableSeats -= count;
    }

    public void releaseSeats(int count) {
        this.availableSeats = Math.min(totalSeats, availableSeats + count);
    }
}
