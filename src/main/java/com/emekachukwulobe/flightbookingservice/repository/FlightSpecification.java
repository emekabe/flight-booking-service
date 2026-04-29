package com.emekachukwulobe.flightbookingservice.repository;

import com.emekachukwulobe.flightbookingservice.domain.Flight;
import com.emekachukwulobe.flightbookingservice.domain.enums.FareClass;
import com.emekachukwulobe.flightbookingservice.domain.enums.FlightStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Composable JPA Specifications for dynamic flight search.
 * Each static method returns an independent predicate that the service layer combines.
 */
public final class FlightSpecification {

    private FlightSpecification() {}

    public static Specification<Flight> belongsToTenant(UUID tenantId) {
        return (root, query, cb) ->
            cb.equal(root.get("tenant").get("id"), tenantId);
    }

    public static Specification<Flight> hasStatus(FlightStatus status) {
        return (root, query, cb) ->
            cb.equal(root.get("status"), status);
    }

    public static Specification<Flight> hasOrigin(String origin) {
        return (root, query, cb) ->
            cb.equal(cb.upper(root.get("origin")), origin.toUpperCase());
    }

    public static Specification<Flight> hasDestination(String destination) {
        return (root, query, cb) ->
            cb.equal(cb.upper(root.get("destination")), destination.toUpperCase());
    }

    public static Specification<Flight> departsOn(LocalDate date) {
        return (root, query, cb) -> {
            OffsetDateTime start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime end   = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
            return cb.and(
                cb.greaterThanOrEqualTo(root.get("departureTime"), start),
                cb.lessThan(root.get("departureTime"), end)
            );
        };
    }

    public static Specification<Flight> hasFareClass(FareClass fareClass) {
        return (root, query, cb) -> {
            Join<Object, Object> fares = root.join("fares");
            return cb.equal(fares.get("fareClass"), fareClass);
        };
    }

    public static Specification<Flight> hasAvailableSeats() {
        return (root, query, cb) -> {
            Join<Object, Object> inventory = root.join("inventory");
            return cb.greaterThan(inventory.get("availableSeats"), 0);
        };
    }

    /**
     * Builds a combined specification from nullable search parameters.
     * Only non-null parameters are added as predicates.
     */
    public static Specification<Flight> buildSearchSpec(
            UUID tenantId,
            String origin,
            String destination,
            LocalDate departureDate,
            FareClass fareClass
    ) {
        // Start with mandatory predicates; typed variable avoids overload ambiguity with PredicateSpecification
        Specification<Flight> spec = belongsToTenant(tenantId)
            .and(hasStatus(FlightStatus.SCHEDULED))
            .and(hasAvailableSeats());

        if (origin != null)        spec = spec.and(hasOrigin(origin));
        if (destination != null)   spec = spec.and(hasDestination(destination));
        if (departureDate != null) spec = spec.and(departsOn(departureDate));
        if (fareClass != null)     spec = spec.and(hasFareClass(fareClass));

        return spec;
    }
}
