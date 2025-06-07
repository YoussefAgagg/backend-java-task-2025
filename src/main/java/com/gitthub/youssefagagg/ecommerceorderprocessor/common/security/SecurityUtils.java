package com.gitthub.youssefagagg.ecommerceorderprocessor.common.security;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utility class for Spring Security.
 *
 * @author Youssef Agagg
 */
public final class SecurityUtils {

  private SecurityUtils() {
  }

  /**
   * Get the userId of the current user.
   *
   * @return the userId of the current user.
   */
  public static Optional<String> getCurrentUserUserName() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
  }


  /**
   * Get the token of the current user.
   *
   * @return the token of the current user.
   */
  @SuppressWarnings("unused")
  public static Optional<String> getCurrentUserToken() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return Optional.ofNullable(extractToken(securityContext.getAuthentication()));
  }

  private static String extractPrincipal(Authentication authentication) {
    if (authentication == null) {
      return null;
    } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
      return springSecurityUser.getUsername();
    } else if (authentication.getPrincipal() instanceof String authenticationPrincipal) {
      return authenticationPrincipal;
    }
    return null;
  }

  @SuppressWarnings("checkstyle:LineLength")
  private static String extractToken(Authentication authentication) {
    if (authentication == null) {
      return null;
    } else if (authentication instanceof UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) {
      return (String) usernamePasswordAuthenticationToken.getCredentials();
    }
    return null;
  }

  /**
   * get current user authorities.
   *
   * @return current user authorities.
   */
  @SuppressWarnings("unused")
  public static List<String> getCurrentUserAuthorities() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return List.of();
    }
    return getAuthorities(authentication).toList();
  }

  /**
   * Checks if the current user has any of the authorities.
   *
   * @param authorities the authorities to check.
   * @return true if the current user has any of the authorities, false otherwise.
   */
  public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (
        authentication != null && getAuthorities(authentication).anyMatch(
            authority -> Arrays.asList(authorities).contains(authority)));
  }


  /**
   * Checks if the current user has a specific authority.
   *
   * @param authority the authority to check.
   * @return true if the current user has the authority, false otherwise.
   */
  @SuppressWarnings("unused")
  public static boolean hasCurrentUserThisAuthority(String authority) {
    return hasCurrentUserAnyOfAuthorities(authority);
  }

  private static Stream<String> getAuthorities(Authentication authentication) {
    return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
  }


}
