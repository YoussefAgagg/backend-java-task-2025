package com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.jwt;


import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures JWT-based security settings for the application.
 * <br>
 * This class extends the Spring Security's {@link SecurityConfigurerAdapter} to allow integration
 * of a custom {@link JwtFilter} into the security filter chain. It utilizes the provided
 * {@link TokenProvider} to validate and process JWT tokens in incoming requests.
 * <br>
 * The {@link JwtFilter} is added to the {@link HttpSecurity} filter chain at a position prior to
 * {@link UsernamePasswordAuthenticationFilter}. This ensures that JWT-based authentication is
 * performed before Spring Security's username/password authentication mechanism.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@RequiredArgsConstructor
public class JWTConfigurer
    extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

  private final TokenProvider tokenProvider;

  @Override
  public void configure(HttpSecurity http) {
    JwtFilter customFilter = new JwtFilter(tokenProvider);
    http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
  }
}
