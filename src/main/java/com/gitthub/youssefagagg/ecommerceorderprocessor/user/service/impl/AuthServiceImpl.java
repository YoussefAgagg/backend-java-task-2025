package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;


import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.AuthoritiesRole;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.jwt.TokenProvider;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.CreateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.LoginRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.TokenDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UserDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.Role;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.mapper.UserMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.repository.RoleRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.AuthService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing authentication and authorization.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final TokenProvider tokenProvider;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;


  @Override
  @Transactional
  public UserDTO register(CreateUserRequest createUserRequest) {
    log.debug("Request to register user : {}", createUserRequest.getUsername());

    // Check if username already exists
    if (userRepository.existsByUsernameIgnoreCase(createUserRequest.getUsername())) {
      throw new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS,
                                "Username already exists: " + createUserRequest.getUsername());
    }

    // Create new user
    User user = userMapper.createUserRequestToEntity(createUserRequest);
    user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));

    // Set role - only USER role is allowed during registration
    Set<Role> roles = new HashSet<>();
    // Always assign USER role regardless of what's in the createUserRequest
    roleRepository.findByName(AuthoritiesRole.ROLE_USER.getValue())
                  .ifPresent(roles::add);
    user.setRoles(roles);

    // Save user
    user = userRepository.save(user);

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public TokenDTO login(LoginRequest loginRequest) {
    log.debug("Request to authenticate user : {}", loginRequest.getUsername());

    // Get user
    User user = userRepository.findByUsernameIgnoreCase(loginRequest.getUsername())
                              .orElseThrow(() -> new CustomException(ErrorCode.BAD_CREDENTIALS,
                                                                     "User not found with username: "
                                                                     + loginRequest.getUsername()));
    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      throw new CustomException(ErrorCode.BAD_CREDENTIALS,
                                "Invalid password for user: " + loginRequest.getUsername());
    }
    // Create authentication token
    Authentication authentication = createAuthentication(user);

    // Generate JWT token
    String jwt = tokenProvider.createToken(authentication);

    // Build token response
    return TokenDTO.builder()
                   .accessToken(jwt)
                   .tokenType("Bearer")
                   .username(user.getUsername())
                   .email(user.getEmail())
                   .firstName(user.getFirstName())
                   .lastName(user.getLastName())
                   .build();
  }





  /**
   * Create an Authentication object for the given user.
   *
   * @param user the user to create authentication for
   * @return the Authentication object
   */
  private Authentication createAuthentication(User user) {
    // Get user roles
    String authorities = user.getRoles().stream()
                             .map(Role::getName)
                             .reduce((a, b) -> a + "," + b)
                             .orElse("");

    // Create authentication token
    org.springframework.security.core.userdetails.User principal =
        new org.springframework.security.core.userdetails.User(user.getUsername(), "",
                                                               AuthoritiesRole.ROLE_USER.getValue()
                                                                                        .equals(
                                                                                            authorities)
                                                               ?
                                                               Set.of()
                                                               : Set.of(() -> authorities));

    return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
  }
}
