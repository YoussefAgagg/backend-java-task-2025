package com.gitthub.youssefagagg.ecommerceorderprocessor.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for caching used by rate limiters.
 */
@Configuration
@EnableCaching
public class CacheConfig {

  /**
   * Cache manager for rate limiting.
   *
   * @return the cache manager
   */
  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("rateLimit");
    cacheManager.setCaffeine(Caffeine.newBuilder()
                                     .expireAfterWrite(1, TimeUnit.HOURS)
                                     .maximumSize(10_000));
    return cacheManager;
  }
}