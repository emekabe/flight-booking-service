package com.emekachukwulobe.flightbookingservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flightBookingOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Flight Booking Service API")
                .version("1.0.0")
                .description("Multi-tenant flight booking and management system. " +
                    "All endpoints (except /api/v1/payments/webhook) require HTTP Basic Authentication. " +
                    "Tenant isolation is enforced automatically based on the authenticated user.")
                .contact(new Contact()
                    .name("Flight Booking Platform")
                    .email("support@flightbooking.io")))
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
            .components(new Components()
                .addSecuritySchemes("basicAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")
                        .description("Use your username and password")));
    }
}
