package com.gitthub.youssefagagg.ecommerceorderprocessor.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuration class for setting up CORS in the application.
 *
 * <p></p>
 * This class is marked as a Spring configuration class using the {@code @Configuration} annotation
 * and is responsible for creating a CORS filter bean. It configures the allowed origins, headers,
 * methods, and exposed headers for cross-origin requests.
 *
 * <p></p>
 * The {@code corsFilter} method initializes and configures a {@link CorsFilter} bean which is used
 * to handle CORS requests across the application. It sets the allowed origins to all domains, the
 * allowed headers to all headers, the allowed methods to GET, POST, PUT, and DELETE, and the
 * exposed headers to all headers.
 */
@Configuration
public class CorsConfig {
  /**
   * Configures and provides a {@link CorsFilter} bean to handle Cross-Origin Resource Sharing
   * (CORS) requests in the application. This filter sets the allowed origins, headers, methods, and
   * exposed headers for all incoming requests.
   *
   * @return a {@link CorsFilter} instance configured with the specified CORS policies.
   */
  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedOrigins(List.of("*"));
    corsConfiguration.setAllowedHeaders(List.of("*"));
    corsConfiguration.setExposedHeaders(List.of("*"));
    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);
    return new CorsFilter(source);
  }
}
