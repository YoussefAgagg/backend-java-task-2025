package com.gitthub.youssefagagg.ecommerceorderprocessor.service;

import com.gitthub.youssefagagg.ecommerceorderprocessor.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Service for managing API rate limits.
 */
@Service
public class RateLimitService {

  private final RateLimitConfig rateLimitConfig;
  private final Map<String, Map<String, Bucket>> buckets = new ConcurrentHashMap<>();

  public RateLimitService(RateLimitConfig rateLimitConfig) {
    this.rateLimitConfig = rateLimitConfig;
  }

  /**
   * Get or create a rate limit bucket for the given key and path.
   *
   * @param key  the key to identify the client (e.g., IP address)
   * @param path the request path
   * @return the bucket for rate limiting
   */
  public Bucket resolveBucket(String key, String path) {
    // Get or create the map of buckets for this client
    Map<String, Bucket> clientBuckets = buckets.computeIfAbsent(key,
                                                                k -> new ConcurrentHashMap<>());

    // Find the appropriate endpoint configuration
    String matchingEndpoint = findMatchingEndpoint(path);

    // Get or create the bucket for this endpoint
    return clientBuckets.computeIfAbsent(matchingEndpoint,
                                         this::createBucket);
  }

  /**
   * Find the endpoint configuration that matches the given path.
   *
   * @param path the request path
   * @return the matching endpoint key or "default" if none matches
   */
  private String findMatchingEndpoint(String path) {
    // Check for exact matches first
    if (rateLimitConfig.getEndpoints().containsKey(path)) {
      return path;
    }

    // Check for pattern matches
    for (String pattern : rateLimitConfig.getEndpoints().keySet()) {
      if (pathMatches(path, pattern)) {
        return pattern;
      }
    }

    // Default if no match found
    return "default";
  }

  /**
   * Check if a path matches a pattern. Simple implementation that supports * as a wildcard.
   *
   * @param path    the request path
   * @param pattern the pattern to match against
   * @return true if the path matches the pattern
   */
  private boolean pathMatches(String path, String pattern) {
    if (pattern.endsWith("*")) {
      String prefix = pattern.substring(0, pattern.length() - 1);
      return path.startsWith(prefix);
    }
    return path.equals(pattern);
  }

  /**
   * Create a new bucket with the configured rate limit for the given endpoint.
   *
   * @param endpoint the endpoint key
   * @return a new bucket
   */
  private Bucket createBucket(String endpoint) {
    int capacity;
    int refillTokens;
    int refillDuration;

    if ("default".equals(endpoint)) {
      // Use default configuration
      capacity = rateLimitConfig.getCapacity();
      refillTokens = rateLimitConfig.getRefillTokens();
      refillDuration = rateLimitConfig.getRefillDuration();
    } else {
      // Use endpoint-specific configuration
      RateLimitConfig.EndpointLimit limit = rateLimitConfig.getEndpoints().get(endpoint);
      capacity = limit.getCapacity();
      refillTokens = limit.getRefillTokens();
      refillDuration = limit.getRefillDuration();
    }

    Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(refillTokens, Duration.ofSeconds(
        refillDuration)));
    return Bucket.builder().addLimit(limit).build();
  }
}
