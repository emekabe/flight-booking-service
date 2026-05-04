package com.emekachukwulobe.flightbookingservice.controller;

import com.emekachukwulobe.flightbookingservice.dto.request.LoginRequest;
import com.emekachukwulobe.flightbookingservice.dto.response.ApiResponse;
import com.emekachukwulobe.flightbookingservice.dto.response.LoginResponse;
import com.emekachukwulobe.flightbookingservice.security.TenantAwareUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and token management")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    @Operation(
        summary = "Login",
        description = "Authenticates a user and returns a Basic Auth token in the 'Authorization' response header. " +
            "Use the returned header value directly in subsequent requests."
    )
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        TenantAwareUserDetails userDetails = (TenantAwareUserDetails) auth.getPrincipal();

        String credentials = request.getUsername() + ":" + request.getPassword();
        String basicToken = "Basic " + Base64.getEncoder()
            .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        LoginResponse body = LoginResponse.builder()
            .username(userDetails.getUsername())
            .role(userDetails.getUser().getRole())
            .userId(userDetails.getUserId())
            .tenantId(userDetails.getTenantId())
            .tenantCode(userDetails.getUser().getTenant().getCode())
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, basicToken)
            .body(ApiResponse.success("Login successful", body));
    }
}
