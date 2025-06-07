package com.gitthub.youssefagagg.ecommerceorderprocessor.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides security-related beans for the application.
 *
 * <p></p>
 * This class is responsible for configuring and initializing security beans required by the
 * application. It includes configuration for components such as password encoders.
 */
@Configuration
public class SecurityBean {
  /**
   * Creates and returns a PasswordEncoder bean for encoding passwords with the BCrypt hashing
   * function.
   *
   * @return an instance of {@link BCryptPasswordEncoder} to handle password encoding.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
