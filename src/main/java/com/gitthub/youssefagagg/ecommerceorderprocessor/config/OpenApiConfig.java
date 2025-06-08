package com.gitthub.youssefagagg.ecommerceorderprocessor.config;

import static com.gitthub.youssefagagg.ecommerceorderprocessor.util.Constants.OPEN_API_SECURITY_REQUIREMENT;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration.
 */
@OpenAPIDefinition(
    info = @Info(
        title = "Rouh Matrouh API",
        version = "1.0",
        description = "API documentation for Rouh Matrouh application"
    ),
    tags = {
        // App Version Module
        @Tag(name = "App Version - Public",
             description = "Public API for retrieving app version information"),
        @Tag(name = "App Version - Management",
             description = "Admin API for managing app versions"),
        // User Module
        @Tag(name = "User - User-Profile",
             description = "API for login user operations"),
        @Tag(name = "User - User Management",
             description = "Admin API for managing users"),
        @Tag(name = "User - Auth",
             description = "API for authentication and authorization"),

        // Contact Us User Module
        @Tag(name = "Contact Us - Forms",
             description = "Public API for submitting contact us forms"),
        @Tag(name = "Contact Us - Forms Management",
             description = "Admin API for managing contact us forms"),


        // Property Module
        @Tag(name = "Property - Amenity",
             description = "Public API for browsing amenities"),
        @Tag(name = "Property - Amenity Management",
             description = "Admin API for managing amenities"),
        @Tag(name = "Property - Property public",
             description = "Public API for browsing properties"),
        @Tag(name = "Property - Property Management",
             description = "Admin API for managing properties"),
        @Tag(name = "Property - Property Owner",
             description = "Owner API for managing their properties"),


        // Media Module
        @Tag(name = "Media",
             description = "Public API for accessing media"),
        @Tag(name = "Media Management",
             description = "Admin API for managing media"),

        // Review Module
        @Tag(name = "Review",
             description = "Public API for browsing reviews"),
        @Tag(name = "Review Management",
             description = "Admin API for managing reviews"),

        // Matrouh Guide Module
        @Tag(name = "MatrouhGuide - Matrouh Guide",
             description = "Public API for browsing Matrouh guide items"),
        @Tag(name = "MatrouhGuide - Matrouh Guide Management",
             description = "Admin API for managing Matrouh guide items"),
        @Tag(name = "MatrouhGuide - Matrouh Guide Owner",
             description = "Owner API for managing their Matrouh guide items"),

        // Transportation Module
        @Tag(name = "Transportation - Transportation",
             description = "Public API for browsing transportation"),
        @Tag(name = "Transportation - Transportation Management",
             description = "Admin API for managing transportation"),
        @Tag(name = "Transportation - Transportation Owner",
             description = "Owner API for managing their transportation"),

        // Safari Module
        @Tag(name = "Safari - Journey Features",
             description = "Public API for browsing journey features"),
        @Tag(name = "Safari - Journey Features Management",
             description = "Admin API for managing journey features"),
        @Tag(name = "Safari - Safari",
             description = "Public API for browsing safaris"),
        @Tag(name = "Safari - Safari Management",
             description = "Admin API for managing safaris"),
        @Tag(name = "Safari - Safari Owner",
             description = "Owner API for managing their safaris"),


        // Express Module - Shop Categories
        @Tag(name = "Express - Shop Categories",
             description = "Public API for browsing shop categories"),
        @Tag(name = "Express - Shop Categories Management",
             description = "Admin API for managing shop categories"),

        // Express Module - Shops
        @Tag(name = "Express - Shops",
             description = "Public API for browsing shops"),
        @Tag(name = "Express - Shops Management",
             description = "Admin API for managing shops"),
        @Tag(name = "Express - Shops Owner",
             description = "Owner API for managing their shops"),

        // Express Module - Product Categories
        @Tag(name = "Express - Product Categories",
             description = "Public API for browsing product categories"),
        @Tag(name = "Express - Product Categories Management",
             description = "Admin API for managing product categories"),


        // Express Module - Products
        @Tag(name = "Express - Products",
             description = "Public API for browsing products"),
        @Tag(name = "Express - Products Management",
             description = "Admin API for managing products"),
        @Tag(name = "Express - Products Owner",
             description = "Owner API for managing their products"),


        // Express Module - Orders
        @Tag(name = "Express - Orders",
             description = "Public API for placing and managing orders"),
        @Tag(name = "Express - Orders Management",
             description = "Admin API for managing orders"),

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
