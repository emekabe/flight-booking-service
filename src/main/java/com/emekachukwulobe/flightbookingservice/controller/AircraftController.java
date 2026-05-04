package com.emekachukwulobe.flightbookingservice.controller;

import com.emekachukwulobe.flightbookingservice.dto.request.CreateAircraftRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.AircraftResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.ApiResponse;
import com.emekachukwulobe.flightbookingservice.security.SecurityUtils;
import com.emekachukwulobe.flightbookingservice.service.AircraftService;
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
@RequestMapping("/api/v1/aircraft")
@RequiredArgsConstructor
@Tag(name = "Aircraft", description = "Aircraft management and seat layout configuration")
public class AircraftController {

    private final AircraftService aircraftService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create aircraft", description = "Creates a new aircraft with its full seat layout. ADMIN only.")
    public ResponseEntity<ApiResponse<AircraftResponse>> createAircraft(
            @Valid @RequestBody CreateAircraftRequest request) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Aircraft created successfully", aircraftService.createAircraft(request, tenantId)));
    }

    @GetMapping
    @Operation(summary = "List aircraft", description = "Returns all aircraft for the current tenant.")
    public ResponseEntity<ApiResponse<Page<AircraftResponse>>> listAircraft(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(aircraftService.listAircraft(tenantId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get aircraft by ID")
    public ResponseEntity<ApiResponse<AircraftResponse>> getAircraft(@PathVariable UUID id) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(aircraftService.getAircraft(id, tenantId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update aircraft", description = "Updates aircraft model and replaces its seat layout. ADMIN only.")
    public ResponseEntity<ApiResponse<AircraftResponse>> updateAircraft(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAircraftRequest request) {
        UUID tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(ApiResponse.success(aircraftService.updateAircraft(id, request, tenantId)));
    }
}
