package com.emekachukwulobe.flightbookingservice.domain.enums;

/**
 * Defines how seat numbers are labelled on an aircraft.
 * AIRLINE: row + letter  e.g. 1A, 12C
 * SEQUENTIAL: simple integers e.g. 1, 2, 3
 */
public enum SeatNamingFormat {
    AIRLINE,
    SEQUENTIAL
}
