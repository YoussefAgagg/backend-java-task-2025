package com.gitthub.youssefagagg.ecommerceorderprocessor.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for rate limiting.
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
@Data
public class RateLimitConfig {

  /**
   * Default capacity (maximum number of requests allowed in the time window).
   */
  private int capacity = 20;

  /**
   * Default number of tokens to refill in each period.
   */
  private int refillTokens = 20;

  /**
   * Default duration in seconds for token refill.
   */
  private int refillDuration = 60;

  /**
   * Endpoint-specific rate limit configurations. Key is the endpoint path pattern, value is the
   * configuration.
   */
  private Map<String, EndpointLimit> endpoints = new HashMap<>();

  /**
   * Configuration for a specific endpoint.
   */
  @Data
  public static class EndpointLimit {
    /**
     * Capacity (maximum number of requests allowed in the time window).
     */
    private int capacity = 20;

    /**
     * Number of tokens to refill in each period.
     */
    private int refillTokens = 20;

    /**
     * Duration in seconds for token refill.
     */
    private int refillDuration = 60;
  }
}