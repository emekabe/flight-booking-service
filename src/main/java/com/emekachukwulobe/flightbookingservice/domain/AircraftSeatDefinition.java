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

/**
 * Defines one seat position in an aircraft's layout.
 * For AIRLINE format: rowNumber=12, seatPosition="A" → "12A"
 * For SEQUENTIAL format: rowNumber=1, seatPosition=null → sequential counter used at generation time
 */
@Entity
@Table(name = "aircraft_seat_definitions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AircraftSeatDefinition extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    /**
     * Letter within the row for AIRLINE format (e.g. "A", "B", "C").
     * Null for SEQUENTIAL format.
     */
    @Column(name = "seat_position", length = 5)
    private String seatPosition;

    /** Which cabin class this seat belongs to. */
    @Enumerated(EnumType.STRING)
    @Column(name = "cabin_class", nullable = false, length = 20)
    private FareClass cabinClass;

    /** False for non-existent seats (e.g. emergency row, missing position). */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
