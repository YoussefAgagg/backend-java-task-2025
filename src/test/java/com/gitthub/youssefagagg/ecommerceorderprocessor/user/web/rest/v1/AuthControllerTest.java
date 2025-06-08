package com.gitthub.youssefagagg.ecommerceorderprocessor.user.web.rest.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.TestcontainersConfiguration;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.CreateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.LoginRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AuthService authService;

  private CreateUserRequest createUserRequest;
  private LoginRequest loginRequest;

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

  }

  @Test
  @DisplayName("Should register a new user successfully")
  void shouldRegisterUserSuccessfully() throws Exception {
    // When/Then
    mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").isNotEmpty())
           .andExpect(jsonPath("$.username").value(createUserRequest.getUsername()))
           .andExpect(jsonPath("$.firstName").value(createUserRequest.getFirstName()))
           .andExpect(jsonPath("$.lastName").value(createUserRequest.getLastName()))
           .andExpect(jsonPath("$.email").value(createUserRequest.getEmail()))
           .andExpect(jsonPath("$.phone").value(createUserRequest.getPhone()));
  }

  @Test
  @DisplayName("Should return 400 when registration request has invalid data")
  void shouldReturn400WhenRegistrationRequestHasInvalidData() throws Exception {
    // Given
    CreateUserRequest invalidRequest = new CreateUserRequest();
    invalidRequest.setUsername("u"); // username min size is 3
    invalidRequest.setPassword("123"); // password min size is 4
    invalidRequest.setFirstName(null); // firstName is @NotNull
    invalidRequest.setLastName("User");
    invalidRequest.setEmail("invalid-email"); // invalid email format
    invalidRequest.setPhone("123"); // invalid phone format

    // When/Then
    mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
           .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should return 400 when username already exists")
  void shouldReturn400WhenUsernameAlreadyExists() throws Exception {
    // Given
    // Register a user first
    authService.register(createUserRequest);

    // Try to register with the same username
    CreateUserRequest duplicateRequest = new CreateUserRequest();
    duplicateRequest.setUsername(createUserRequest.getUsername());
    duplicateRequest.setPassword("anotherpassword");
    duplicateRequest.setFirstName("Another");
    duplicateRequest.setLastName("User");
    duplicateRequest.setEmail("another@example.com");
    duplicateRequest.setPhone("+9876543210");

    // When/Then
    mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
           .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should login successfully")
  void shouldLoginSuccessfully() throws Exception {
    // Given
    // Register a user first
    authService.register(createUserRequest);

    // When/Then
    mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.accessToken").isNotEmpty())
           .andExpect(jsonPath("$.tokenType").value("Bearer"))
           .andExpect(jsonPath("$.username").value(loginRequest.getUsername()));
  }

  @Test
  @DisplayName("Should return 400 when login request has invalid data")
  void shouldReturn400WhenLoginRequestHasInvalidData() throws Exception {
    // Given
    LoginRequest invalidRequest = new LoginRequest();
    invalidRequest.setUsername(""); // username is @NotBlank
    invalidRequest.setPassword("123"); // password min size is 4

    // When/Then
    mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
           .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should return 400 when login credentials are invalid")
  void shouldReturn400WhenLoginCredentialsAreInvalid() throws Exception {
    // Given
    // Register a user first
    authService.register(createUserRequest);

    // Try to login with wrong password
    LoginRequest wrongPasswordRequest = new LoginRequest();
    wrongPasswordRequest.setUsername(createUserRequest.getUsername());
    wrongPasswordRequest.setPassword("wrongpassword");

    // When/Then
    mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
           .andExpect(status().isBadRequest());
  }
}