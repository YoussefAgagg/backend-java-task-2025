package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gitthub.youssefagagg.ecommerceorderprocessor.TestcontainersConfiguration;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.ChangePasswordRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.CreateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.LoginRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.TokenDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.UpdateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.UserDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.RoleRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.AuthService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class UserServiceIT {

  @Autowired
  private UserService userService;

  @Autowired
  private AuthService authService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  private CreateUserRequest createUserRequest;
  private UpdateUserRequest updateUserRequest;
  private ChangePasswordRequest changePasswordRequest;
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

    updateUserRequest = new UpdateUserRequest();
    updateUserRequest.setFirstName("Updated");
    updateUserRequest.setLastName("User");
    updateUserRequest.setEmail("updated@example.com");
    updateUserRequest.setPhone("+9876543210");

    changePasswordRequest = new ChangePasswordRequest();
    changePasswordRequest.setCurrentPassword("password123");
    changePasswordRequest.setNewPassword("newpassword123");

    httpServletRequest = new MockHttpServletRequest();

    // Clear security context
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(String username) {
    // Create authentication token for the user
    org.springframework.security.core.userdetails.User principal =
        new org.springframework.security.core.userdetails.User(username, "",
                                                               Collections.emptyList());
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  @DisplayName("Should get current user successfully")
  void shouldGetCurrentUserSuccessfully() {
    // Given
    UserDTO registeredUser = authService.register(createUserRequest);
    authenticateUser(registeredUser.getUsername());

    // When
    UserDTO result = userService.getCurrentUser();

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo(createUserRequest.getUsername());
    assertThat(result.getFirstName()).isEqualTo(createUserRequest.getFirstName());
    assertThat(result.getLastName()).isEqualTo(createUserRequest.getLastName());
    assertThat(result.getEmail()).isEqualTo(createUserRequest.getEmail());
    assertThat(result.getPhone()).isEqualTo(createUserRequest.getPhone());
  }

  @Test
  @DisplayName("Should throw exception when user not authenticated during getCurrentUser")
  void shouldThrowExceptionWhenUserNotAuthenticatedDuringGetCurrentUser() {
    // Given - no authentication

    // When/Then
    assertThatThrownBy(() -> userService.getCurrentUser())
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHENTICATION_CREDENTIALS_NOT_FOUND);
  }

  @Test
  @DisplayName("Should update current user successfully")
  void shouldUpdateCurrentUserSuccessfully() {
    // Given
    UserDTO registeredUser = authService.register(createUserRequest);
    authenticateUser(registeredUser.getUsername());

    // When
    UserDTO result = userService.updateCurrentUser(updateUserRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo(createUserRequest.getUsername());
    assertThat(result.getFirstName()).isEqualTo(updateUserRequest.getFirstName());
    assertThat(result.getLastName()).isEqualTo(updateUserRequest.getLastName());
    assertThat(result.getEmail()).isEqualTo(updateUserRequest.getEmail());
    assertThat(result.getPhone()).isEqualTo(updateUserRequest.getPhone());

    // Verify changes are persisted
    User updatedUser = userRepository.findByUsernameIgnoreCase(createUserRequest.getUsername())
                                     .orElseThrow();
    assertThat(updatedUser.getFirstName()).isEqualTo(updateUserRequest.getFirstName());
    assertThat(updatedUser.getLastName()).isEqualTo(updateUserRequest.getLastName());
    assertThat(updatedUser.getEmail()).isEqualTo(updateUserRequest.getEmail());
    assertThat(updatedUser.getPhone()).isEqualTo(updateUserRequest.getPhone());
  }


  @Test
  @DisplayName("Should change password successfully")
  void shouldChangePasswordSuccessfully() {
    // Given
    UserDTO registeredUser = authService.register(createUserRequest);
    authenticateUser(registeredUser.getUsername());

    // When
    userService.changePassword(changePasswordRequest);

    // Then
    // Try to login with new password
    LoginRequest loginWithNewPassword = new LoginRequest();
    loginWithNewPassword.setUsername(createUserRequest.getUsername());
    loginWithNewPassword.setPassword(changePasswordRequest.getNewPassword());

    TokenDTO tokenDTO = authService.login(loginWithNewPassword);
    assertThat(tokenDTO).isNotNull();
    assertThat(tokenDTO.getAccessToken()).isNotBlank();
  }

  @Test
  @DisplayName("Should throw exception when current password is invalid during changePassword")
  void shouldThrowExceptionWhenCurrentPasswordIsInvalidDuringChangePassword() {
    // Given
    UserDTO registeredUser = authService.register(createUserRequest);
    authenticateUser(registeredUser.getUsername());

    // Create change password request with wrong current password
    ChangePasswordRequest wrongPasswordRequest = new ChangePasswordRequest();
    wrongPasswordRequest.setCurrentPassword("wrongpassword");
    wrongPasswordRequest.setNewPassword("newpassword123");

    // When/Then
    assertThatThrownBy(() -> userService.changePassword(wrongPasswordRequest))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_CREDENTIALS);
  }
}