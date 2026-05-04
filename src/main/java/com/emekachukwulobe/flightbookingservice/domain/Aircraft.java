package com.emekachukwulobe.flightbookingservice.domain;

import com.emekachukwulobe.flightbookingservice.domain.enums.SeatNamingFormat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "aircraft",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_aircraft_registration_tenant",
        columnNames = {"registration_number", "tenant_id"}
    )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Aircraft extends BaseEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    /** e.g. NG-ABC or 5N-AAA */
    @Column(name = "registration_number", nullable = false, length = 20)
    private String registrationNumber;

    /** e.g. Boeing 737-800, Airbus A320 */
    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_naming_format", nullable = false, length = 20)
    private SeatNamingFormat seatNamingFormat;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Full seat layout — one entry per physical seat position. */
    @OneToMany(mappedBy = "aircraft", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AircraftSeatDefinition> seatDefinitions = new ArrayList<>();
}
