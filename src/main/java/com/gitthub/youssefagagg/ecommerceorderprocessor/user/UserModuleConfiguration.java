package com.gitthub.youssefagagg.ecommerceorderprocessor.user;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration class for the User module. This class defines the module boundaries and
 * configuration for the User domain.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.gitthub.youssefagagg.ecommerceorderprocessor.user.repository")
@EnableTransactionManagement
public class UserModuleConfiguration {
  // Module-specific configuration
}