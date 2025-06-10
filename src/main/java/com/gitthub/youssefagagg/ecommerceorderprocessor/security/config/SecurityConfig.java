package com.gitthub.youssefagagg.ecommerceorderprocessor.security.config;


import com.gitthub.youssefagagg.ecommerceorderprocessor.security.AuthoritiesRole;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.jwt.JWTConfigurer;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.jwt.JwtAuthenticationEntryPoint;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.jwt.JwtAuthorizationEntryPoint;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;


/**
 * This configuration handles api web requests with stateless session.
 *
 * @author Youssef Agagg
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationEntryPoint unauthorizedHandler;
  private final JwtAuthorizationEntryPoint forbiddenHandler;
  private final TokenProvider tokenProvider;
  private final CorsFilter corsFilter;

  /**
   * Configures a security filter chain for API endpoints. This method sets up security
   * configurations such as disabling CSRF, adding CORS filters, handling exceptions, and managing
   * user session creation. It also defines access control for various HTTP methods and URL patterns
   * based on user roles and permissions.
   *
   * @param http the {@link HttpSecurity} object to configure the security behaviors for HTTP
   *             requests.
   * @return a {@link SecurityFilterChain} defining the security configurations for the application.
   * @throws Exception if an error occurs during the construction of the security filter chain.
   */
  @Bean
  public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

    http
        .csrf(AbstractHttpConfigurer::disable)
        .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
        .anonymous(AbstractHttpConfigurer::disable)
        .exceptionHandling(ex -> {
          ex.authenticationEntryPoint(unauthorizedHandler);
          ex.accessDeniedHandler(forbiddenHandler);
        })
        .sessionManagement(se -> se.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
                                   .requestMatchers("/static/**").permitAll()
                                   .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                   .requestMatchers("/management/health").permitAll()
                                   .requestMatchers("/management/**").hasAuthority(
                                       AuthoritiesRole.ROLE_ADMIN.getValue())
                                   // Auth endpoints - public access
                                   .requestMatchers("/api/v1/auth/register").permitAll()
                                   .requestMatchers("/api/v1/auth/login").permitAll()
                                   // Public endpoints - public access
                                   .requestMatchers(HttpMethod.GET, "/api/v1/products/**")
                                   .permitAll()
                                   // User profile endpoints - authenticated user access
                                   // Admin endpoints - admin only access
                                   .requestMatchers("/api/v1/admin/**").hasAuthority(
                                       AuthoritiesRole.ROLE_ADMIN.getValue())
                                   .anyRequest().authenticated()
                              )
        .with(securityConfigurerAdapter(), Customizer.withDefaults());

    return http.build();
  }


  private JWTConfigurer securityConfigurerAdapter() {
    return new JWTConfigurer(tokenProvider);
  }


}
