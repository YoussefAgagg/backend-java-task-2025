package com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Properties specific to JWT.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

  /**
   * Secret key for signing and verifying JWT tokens.
   */
  private String secret;

  /**
   * Token expiration time in seconds.
   */
  private long expiration;

  /**
   * Issuer of the token.
   */
  private String issuer;

  private long refreshTokenExpiration;
}