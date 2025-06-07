package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;


import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.Role;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Spring Security's {@link UserDetailsService} to load user-specific data. This
 * service retrieves user information from the database and converts it to Spring Security's
 * {@link UserDetails} format for authentication and authorization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * Loads a user by username for authentication purposes.
   *
   * @param username the username to load
   * @return a UserDetails object containing the user's credentials and authorities
   * @throws UsernameNotFoundException if the user is not found
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("Authenticating user: {}", username);

    return userRepository.findByUsernameIgnoreCaseAndDeletedIsFalse(username)
                         .map(this::createSpringSecurityUser)
                         .orElseThrow(() -> new UsernameNotFoundException(
                             "User " + username + " was not found in the database"));
  }

  /**
   * Converts a domain User entity to a Spring Security UserDetails object.
   *
   * @param user the domain user entity
   * @return a Spring Security UserDetails object
   */
  private org.springframework.security.core.userdetails.User createSpringSecurityUser(User user) {

    // Extract authorities from user roles
    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                                                   .map(Role::getName)
                                                   .map(SimpleGrantedAuthority::new)
                                                   .collect(Collectors.toList());

    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),
        user.getPassword(),
        authorities
    );
  }
}