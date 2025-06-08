package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gitthub.youssefagagg.ecommerceorderprocessor.TestcontainersConfiguration;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.AuthoritiesRole;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.CreateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.LoginRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.TokenDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UserDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.repository.RoleRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class AuthServiceIT {

  @Autowired
  private AuthService authService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  private CreateUserRequest createUserRequest;
  private LoginRequest loginRequest;
  private HttpServletRequest httpServletRequest;

  @BeforeEach
  void setUp() {


    // Setup test data
    createUserRequest = new CreateUserRequest();
    createUserRequest.setUsername("testuser");
    createUserRequest.setPassword("password123");
    createUserRequest.setFirstName("Test");
    createUserRequest.setLastName("User");
    createUserRequest.setEmail("test@example.com");
    createUserRequest.setPhone("+1234567890");

    loginRequest = new LoginRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("password123");

    httpServletRequest = new MockHttpServletRequest();
  }

  @Test
  @DisplayName("Should register a new user successfully")
  void shouldRegisterUserSuccessfully() {
    // When
    UserDTO result = authService.register(createUserRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo(createUserRequest.getUsername());
    assertThat(result.getFirstName()).isEqualTo(createUserRequest.getFirstName());
    assertThat(result.getLastName()).isEqualTo(createUserRequest.getLastName());
    assertThat(result.getEmail()).isEqualTo(createUserRequest.getEmail());
    assertThat(result.getPhone()).isEqualTo(createUserRequest.getPhone());
    assertThat(result.getRoles()).contains(AuthoritiesRole.ROLE_USER.getValue());

    // Verify user is saved in the database
    assertThat(
        userRepository.findByUsernameIgnoreCase(createUserRequest.getUsername())).isPresent();
  }

  @Test
  @DisplayName("Should throw exception when username already exists during registration")
  void shouldThrowExceptionWhenUsernameAlreadyExists() {
    // Given
    authService.register(createUserRequest);

    // Create another user with the same username
    CreateUserRequest duplicateRequest = new CreateUserRequest();
    duplicateRequest.setUsername("testuser"); // Same username
    duplicateRequest.setPassword("password456");
    duplicateRequest.setFirstName("Another");
    duplicateRequest.setLastName("User");
    duplicateRequest.setEmail("another@example.com");
    duplicateRequest.setPhone("+9876543210");

    // When/Then
    assertThatThrownBy(() -> authService.register(duplicateRequest))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("Should login user successfully after registration")
  void shouldLoginUserSuccessfully() {
    // Given
    authService.register(createUserRequest);

    // When
    TokenDTO result = authService.login(loginRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isNotBlank();
    assertThat(result.getTokenType()).isEqualTo("Bearer");
    assertThat(result.getUsername()).isEqualTo(createUserRequest.getUsername());
    assertThat(result.getEmail()).isEqualTo(createUserRequest.getEmail());
    assertThat(result.getFirstName()).isEqualTo(createUserRequest.getFirstName());
    assertThat(result.getLastName()).isEqualTo(createUserRequest.getLastName());
  }

  @Test
  @DisplayName("Should throw exception when user not found during login")
  void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
    // Given - no user registered

    // When/Then
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_CREDENTIALS);
  }

  @Test
  @DisplayName("Should throw exception when password is invalid during login")
  void shouldThrowExceptionWhenPasswordIsInvalidDuringLogin() {
    // Given
    authService.register(createUserRequest);

    // Create login request with wrong password
    LoginRequest wrongPasswordRequest = new LoginRequest();
    wrongPasswordRequest.setUsername("testuser");
    wrongPasswordRequest.setPassword("wrongpassword");

    // When/Then
    assertThatThrownBy(() -> authService.login(wrongPasswordRequest))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_CREDENTIALS);
  }
}
