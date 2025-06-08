package com.gitthub.youssefagagg.ecommerceorderprocessor.user.web.rest.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.TestcontainersConfiguration;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.ChangePasswordRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.CreateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.LoginRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.TokenDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UpdateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UserDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.AuthService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;

  @Autowired
  private AuthService authService;

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

  @AfterEach
  void tearDown() {
    // Clear security context after each test
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
  void shouldGetCurrentUserSuccessfully() throws Exception {
    // Given
    UserDTO registeredUser = authService.register(createUserRequest);
    authenticateUser(registeredUser.getUsername());

    // When/Then
    mockMvc.perform(get("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").isNotEmpty())
           .andExpect(jsonPath("$.username").value(createUserRequest.getUsername()))
           .andExpect(jsonPath("$.firstName").value(createUserRequest.getFirstName()))
           .andExpect(jsonPath("$.lastName").value(createUserRequest.getLastName()))
           .andExpect(jsonPath("$.email").value(createUserRequest.getEmail()))
           .andExpect(jsonPath("$.phone").value(createUserRequest.getPhone()));
  }

  @Test
  @DisplayName("Should return 401 when user not authenticated during getCurrentUser")
  void shouldReturn401WhenUserNotAuthenticatedDuringGetCurrentUser() throws Exception {
    // Given - no authentication

    // When/Then
    mockMvc.perform(get("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should update current user successfully")
  void shouldUpdateCurrentUserSuccessfully() throws Exception {
    // Given
    UserDTO registeredUser = authService.register(createUserRequest);
    authenticateUser(registeredUser.getUsername());

    // When/Then
    mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.firstName").value(updateUserRequest.getFirstName()))
           .andExpect(jsonPath("$.lastName").value(updateUserRequest.getLastName()))
           .andExpect(jsonPath("$.email").value(updateUserRequest.getEmail()))
           .andExpect(jsonPath("$.phone").value(updateUserRequest.getPhone()));
  }

  @Test
  @DisplayName("Should return 400 when update request has invalid data")
  void shouldReturn400WhenUpdateRequestHasInvalidData() throws Exception {
    // Given
    UserDTO registeredUser = authService.register(createUserRequest);
    authenticateUser(registeredUser.getUsername());

    UpdateUserRequest invalidRequest = new UpdateUserRequest();
    invalidRequest.setFirstName(null); // firstName is @NotNull
    invalidRequest.setLastName("User");
    invalidRequest.setEmail("invalid-email"); // invalid email format
    invalidRequest.setPhone("123"); // invalid phone format

    // When/Then
    mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
           .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should change password successfully")
  void shouldChangePasswordSuccessfully() throws Exception {
    // Given
    UserDTO registeredUser = authService.register(createUserRequest);
    authenticateUser(registeredUser.getUsername());

    // When/Then
    mockMvc.perform(post("/api/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
           .andExpect(status().isNoContent());

    // Verify we can login with the new password
    LoginRequest loginWithNewPassword = new LoginRequest();
    loginWithNewPassword.setUsername(createUserRequest.getUsername());
    loginWithNewPassword.setPassword(changePasswordRequest.getNewPassword());

    TokenDTO tokenDTO = authService.login(loginWithNewPassword);
    // No need to assert here as the login would throw an exception if it failed
  }

  @Test
  @DisplayName("Should return 400 when change password request has invalid data")
  void shouldReturn400WhenChangePasswordRequestHasInvalidData() throws Exception {
    // Given
    UserDTO registeredUser = authService.register(createUserRequest);
    authenticateUser(registeredUser.getUsername());

    ChangePasswordRequest invalidRequest = new ChangePasswordRequest();
    invalidRequest.setCurrentPassword(""); // currentPassword is @NotBlank
    invalidRequest.setNewPassword("123"); // newPassword min size is 4

    // When/Then
    mockMvc.perform(post("/api/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
           .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should return 400 when current password is incorrect")
  void shouldReturn400WhenCurrentPasswordIsIncorrect() throws Exception {
    // Given
    UserDTO registeredUser = authService.register(createUserRequest);
    authenticateUser(registeredUser.getUsername());

    // Create change password request with wrong current password
    ChangePasswordRequest wrongPasswordRequest = new ChangePasswordRequest();
    wrongPasswordRequest.setCurrentPassword("wrongpassword");
    wrongPasswordRequest.setNewPassword("newpassword123");

    // When/Then
    mockMvc.perform(post("/api/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
           .andExpect(status().isBadRequest());
  }
}
