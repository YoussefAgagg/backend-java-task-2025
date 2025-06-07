package com.gitthub.youssefagagg.ecommerceorderprocessor.common.config;


import com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.SpringSecurityAuditorAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration class responsible for enabling JPA auditing in the application.
 *
 * <p></p>
 * This class is annotated with {@code @Configuration} to mark it as a Spring configuration class
 * and {@code @EnableJpaAuditing} to enable auditing features in Spring Data JPA. The
 * {@code auditorAwareRef} specifies the bean that provides the current auditor information.
 *
 * <p></p>
 * The {@code auditorAware} method is a bean definition for providing an instance of
 * {@link AuditorAware}, implementing the logic to determine the current auditor for audit fields
 * like createdBy and lastModifiedBy.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableTransactionManagement
public class AuditingConfiguration {
  /**
   * Provides a bean of type {@link AuditorAware} for determining the current auditor in the
   * application. This is used by Spring Data JPA to populate auditing fields like createdBy and
   * lastModifiedBy with the identifier of the currently authenticated user.
   *
   * @return an instance of {@link AuditorAware} that resolves the current auditor using Spring
   *     Security's authentication context.
   */
  @Bean
  public AuditorAware<String> auditorAware() {
    return new SpringSecurityAuditorAware();
  }
}
