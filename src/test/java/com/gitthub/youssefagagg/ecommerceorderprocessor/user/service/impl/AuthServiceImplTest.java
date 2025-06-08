package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock
  private TokenProvider tokenProvider;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;
  private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  private AuthServiceImpl authService;

  private CreateUserRequest createUserRequest;
  private User user;
  private UserDTO userDTO;
  private Role userRole;
  private LoginRequest loginRequest;
  private HttpServletRequest httpServletRequest;

  @BeforeEach
  void setUp() {
    authService = new AuthServiceImpl(tokenProvider, userRepository, roleRepository,
                                      passwordEncoder, userMapper);
    // Setup test data
    createUserRequest = new CreateUserRequest();
    createUserRequest.setUsername("testuser");
    createUserRequest.setPassword("password123");
    createUserRequest.setFirstName("Test");
    createUserRequest.setLastName("User");
    createUserRequest.setEmail("test@example.com");
    createUserRequest.setPhone("+1234567890");

    userRole = new Role();
    userRole.setName(AuthoritiesRole.ROLE_USER.getValue());

    user = User.builder()
               .id(1L)
               .username("testuser")
               .password("encodedPassword")
               .firstName("Test")
               .lastName("User")
               .email("test@example.com")
               .phone("+1234567890")
               .roles(Set.of(userRole))
               .build();

    userDTO = new UserDTO();
    userDTO.setId(1L);
    userDTO.setUsername("testuser");
    userDTO.setFirstName("Test");
    userDTO.setLastName("User");
    userDTO.setEmail("test@example.com");
    userDTO.setPhone("+1234567890");
    userDTO.setRoles(Set.of(AuthoritiesRole.ROLE_USER.getValue()));

    loginRequest = new LoginRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("password123");

    httpServletRequest = mock(HttpServletRequest.class);
  }

  @Test
  @DisplayName("Should register a new user successfully")
  void shouldRegisterUserSuccessfully() {
    // Given
    when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(roleRepository.findByName(AuthoritiesRole.ROLE_USER.getValue())).thenReturn(
        Optional.of(userRole));
    when(userRepository.save(any(User.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

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

    verify(userRepository).existsByUsernameIgnoreCase(createUserRequest.getUsername());
    verify(passwordEncoder).encode(createUserRequest.getPassword());
    verify(roleRepository).findByName(AuthoritiesRole.ROLE_USER.getValue());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw exception when username already exists during registration")
  void shouldThrowExceptionWhenUsernameAlreadyExists() {
    // Given
    when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(true);

    // When/Then
    assertThatThrownBy(() -> authService.register(createUserRequest))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_ALREADY_EXISTS);

    verify(userRepository).existsByUsernameIgnoreCase(createUserRequest.getUsername());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should login user successfully")
  void shouldLoginUserSuccessfully() {
    // Given
    String token = "jwt-token";
    when(userRepository.findByUsernameIgnoreCase(loginRequest.getUsername())).thenReturn(
        Optional.of(user));
    when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
    when(tokenProvider.createToken(any(Authentication.class))).thenReturn(token);

    // When
    TokenDTO result = authService.login(loginRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isEqualTo(token);
    assertThat(result.getTokenType()).isEqualTo("Bearer");
    assertThat(result.getUsername()).isEqualTo(user.getUsername());
    assertThat(result.getEmail()).isEqualTo(user.getEmail());
    assertThat(result.getFirstName()).isEqualTo(user.getFirstName());
    assertThat(result.getLastName()).isEqualTo(user.getLastName());

    verify(userRepository).findByUsernameIgnoreCase(loginRequest.getUsername());
    verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
    verify(tokenProvider).createToken(any(Authentication.class));
  }

  @Test
  @DisplayName("Should throw exception when user not found during login")
  void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
    // Given
    when(userRepository.findByUsernameIgnoreCase(loginRequest.getUsername())).thenReturn(
        Optional.empty());

    // When/Then
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_CREDENTIALS);

    verify(userRepository).findByUsernameIgnoreCase(loginRequest.getUsername());
    verify(tokenProvider, never()).createToken(any());
  }

  @Test
  @DisplayName("Should throw exception when password is invalid during login")
  void shouldThrowExceptionWhenPasswordIsInvalidDuringLogin() {
    // Given
    when(userRepository.findByUsernameIgnoreCase(loginRequest.getUsername())).thenReturn(
        Optional.of(user));
    when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

    // When/Then
    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_CREDENTIALS);

    verify(userRepository).findByUsernameIgnoreCase(loginRequest.getUsername());
    verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
    verify(tokenProvider, never()).createToken(any());
  }
}
