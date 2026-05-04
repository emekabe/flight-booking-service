package com.emekachukwulobe.flightbookingservice.controller;

import com.emekachukwulobe.flightbookingservice.dto.request.CreateTenantRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.SmsConfigRequest;
import com.emekachukwulobe.flightbookingservice.dto.request.SmtpConfigRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.ApiResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.TenantResponse;
import com.emekachukwulobe.flightbookingservice.security.SecurityUtils;
import com.emekachukwulobe.flightbookingservice.service.TenantConfigService;
import com.emekachukwulobe.flightbookingservice.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Tenant (airline) management — ADMIN only")
public class TenantController {

    private final TenantService tenantService;
    private final TenantConfigService tenantConfigService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new tenant", description = "Creates a new airline tenant and seeds its default configuration. ADMIN role required.")
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Tenant created successfully", response));
    }

    @PutMapping("/{id}/config/smtp")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Configure SMTP settings", description = "Sets the SMTP email configuration for the tenant. ADMIN only.")
    public ResponseEntity<ApiResponse<Void>> configureSmtp(
            @PathVariable UUID id,
            @Valid @RequestBody SmtpConfigRequest request) {
        tenantConfigService.upsertConfig(id, "SMTP_HOST",       request.getHost());
        tenantConfigService.upsertConfig(id, "SMTP_PORT",       request.getPort());
        tenantConfigService.upsertConfig(id, "SMTP_USERNAME",   request.getUsername());
        tenantConfigService.upsertConfig(id, "SMTP_PASSWORD",   request.getPassword());
        tenantConfigService.upsertConfig(id, "SMTP_FROM_EMAIL", request.getFromEmail());
        tenantConfigService.upsertConfig(id, "SMTP_FROM_NAME",  request.getFromName());
        return ResponseEntity.ok(ApiResponse.success("SMTP configuration saved", null));
    }

    @PutMapping("/{id}/config/sms")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Configure SMS settings", description = "Sets the Twilio SMS configuration for the tenant. ADMIN only.")
    public ResponseEntity<ApiResponse<Void>> configureSms(
            @PathVariable UUID id,
            @Valid @RequestBody SmsConfigRequest request) {
        tenantConfigService.upsertConfig(id, "SMS_ACCOUNT_SID", request.getAccountSid());
        tenantConfigService.upsertConfig(id, "SMS_AUTH_TOKEN",  request.getAuthToken());
        tenantConfigService.upsertConfig(id, "SMS_FROM_NUMBER", request.getFromNumber());
        return ResponseEntity.ok(ApiResponse.success("SMS configuration saved", null));
    }

    @PatchMapping("/{id}/config/email-enabled")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle email notifications", description = "Enables or disables email notifications for the tenant.")
    public ResponseEntity<ApiResponse<Void>> setEmailEnabled(
            @PathVariable UUID id,
            @RequestParam boolean enabled) {
        tenantConfigService.upsertConfig(id, "EMAIL_ENABLED", String.valueOf(enabled));
        return ResponseEntity.ok(ApiResponse.success("Email notifications " + (enabled ? "enabled" : "disabled"), null));
    }

    @PatchMapping("/{id}/config/sms-enabled")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle SMS notifications", description = "Enables or disables SMS notifications for the tenant.")
    public ResponseEntity<ApiResponse<Void>> setSmsEnabled(
            @PathVariable UUID id,
            @RequestParam boolean enabled) {
        tenantConfigService.upsertConfig(id, "SMS_ENABLED", String.valueOf(enabled));
        return ResponseEntity.ok(ApiResponse.success("SMS notifications " + (enabled ? "enabled" : "disabled"), null));
    }
}
