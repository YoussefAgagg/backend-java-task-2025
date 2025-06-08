package com.gitthub.youssefagagg.ecommerceorderprocessor.security;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;

/**
 * Implementation of {@link AuditorAware} based on Spring Security.
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {

  @Override
  @NonNull
  public Optional<String> getCurrentAuditor() {
    return Optional.of(SecurityUtils.getCurrentUserUserName().orElse("anonymous"));
  }

}
