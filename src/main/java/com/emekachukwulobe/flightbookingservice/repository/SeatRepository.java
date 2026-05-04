package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.Seat;
import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import com.emekachukwulobe.flightbookingservice.domain.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findAllByFlightIdAndStatus(UUID flightId, SeatStatus status);

    List<Seat> findAllByFlightId(UUID flightId);

    Optional<Seat> findByFlightIdAndSeatNumber(UUID flightId, String seatNumber);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("""
        SELECT s FROM Seat s
        WHERE s.flight.id = :flightId
          AND s.cabinClass = :cabinClass
          AND s.status = 'AVAILABLE'
    """)
    List<Seat> findAvailableByFlightIdAndCabinClass(
        @Param("flightId") UUID flightId,
        @Param("cabinClass") FareClass cabinClass
    );

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Seat s WHERE s.flight.id = :flightId AND s.seatNumber = :seatNumber")
    Optional<Seat> findByFlightIdAndSeatNumberWithLock(
        @Param("flightId") UUID flightId,
        @Param("seatNumber") String seatNumber
    );
}
