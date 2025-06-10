package com.gitthub.youssefagagg.ecommerceorderprocessor.config;

import static com.gitthub.youssefagagg.ecommerceorderprocessor.util.Constants.OPEN_API_SECURITY_REQUIREMENT;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration.
 */
@OpenAPIDefinition(
    info = @Info(
        title = "E-Commerce Order Processor API",
        version = "1.0",
        description = "API documentation for e-commerce order processing system"
    ),
    tags = {

    }
)
@Configuration
@SecurityScheme(
    name = OPEN_API_SECURITY_REQUIREMENT,
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER
)
@RequiredArgsConstructor
public class OpenApiConfig {
}
