package com.emekachukwulobe.flightbookingservice.controller;

import com.emekachukwulobe.flightbookingservice.dto.request.CreateFlightRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.SearchFlightRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.UpdateFlightInventoryRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.ApiResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.FlightResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.PagedResponse;
import com.emekachukwulobe.flightbookingservice.security.SecurityUtils;
import com.emekachukwulobe.flightbookingservice.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Flight inventory and search")
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    @Operation(summary = "Search flights", description = "Search available flights with optional filters. Results are cached in Redis.")
    public ResponseEntity<ApiResponse<PagedResponse<FlightResponse>>> searchFlights(
            @ParameterObject SearchFlightRequest request,
            @ParameterObject @PageableDefault(size = 20, sort = "departureTime") Pageable pageable) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        PagedResponse<FlightResponse> results = flightService.searchFlights(request, tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create flight", description = "Creates a new flight. Inventory defaults to 0 seats — call PUT /{id}/inventory to set capacity. ADMIN only.")
    public ResponseEntity<ApiResponse<FlightResponse>> createFlight(
            @Valid @RequestBody CreateFlightRequest request) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        FlightResponse response = flightService.createFlight(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Flight created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update flight details", description = "Updates flight number, route, and schedule. ADMIN only.")
    public ResponseEntity<ApiResponse<FlightResponse>> updateFlight(
            @PathVariable UUID id,
            @Valid @RequestBody CreateFlightRequest request) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(flightService.updateFlight(id, request, tenantId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flight by ID", description = "Returns full flight details including fares and inventory.")
    public ResponseEntity<ApiResponse<FlightResponse>> getFlightById(@PathVariable UUID id) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(flightService.getFlightById(id, tenantId)));
    }

    @PutMapping("/{id}/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update flight inventory", description = "Sets total seat capacity for a flight. Resets available seats to the new total. ADMIN only.")
    public ResponseEntity<ApiResponse<FlightResponse>> updateInventory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFlightInventoryRequest request) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(flightService.updateInventory(id, request, tenantId)));
    }
}
