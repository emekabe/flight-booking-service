package com.emekachukwulobe.flightbookingservice.controller;

import com.emekachukwulobe.flightbookingservice.dto.request.CreateBookingRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.ApiResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.BookingResponse;
import com.emekachukwulobe.flightbookingservice.security.SecurityUtils;
import com.emekachukwulobe.flightbookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "PNR creation, confirmation, and cancellation")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create booking", description = "Creates a PENDING booking and reserves seats. Booking expires after the configured TTL if not confirmed.")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        UUID userId   = SecurityUtils.getCurrentUserId();
        BookingResponse response = bookingService.createBooking(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Booking created successfully", response));
    }

    @GetMapping("/{reference}")
    @Operation(summary = "Get booking", description = "Retrieves a booking by PNR reference within the authenticated tenant.")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable String reference) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBooking(reference, tenantId)));
    }

    @PostMapping("/{reference}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm booking", description = "Transitions booking from PENDING to CONFIRMED and issues tickets. Used for CASH/TRANSFER payments. ADMIN only.")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(@PathVariable String reference) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed", bookingService.confirmBooking(reference, tenantId)));
    }

    @DeleteMapping("/{reference}")
    @Operation(summary = "Cancel booking", description = "Cancels a booking, releases reserved seats, and voids any issued tickets.")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable String reference) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", bookingService.cancelBooking(reference, tenantId)));
    }
}
