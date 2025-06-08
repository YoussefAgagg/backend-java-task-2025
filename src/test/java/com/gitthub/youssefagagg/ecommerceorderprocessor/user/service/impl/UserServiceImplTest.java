package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.ChangePasswordRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.UpdateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.UserDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Role;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.UserMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.AuthoritiesRole;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.impl.UserServiceImpl;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  private UserServiceImpl userService;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  private User user;
  private UserDTO userDTO;
  private UpdateUserRequest updateUserRequest;
  private ChangePasswordRequest changePasswordRequest;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder);
    // Setup security context mock
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn("testuser");

    // Setup test data
    Role userRole = new Role();
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

    updateUserRequest = new UpdateUserRequest();
    updateUserRequest.setFirstName("Updated");
    updateUserRequest.setLastName("User");
    updateUserRequest.setEmail("updated@example.com");
    updateUserRequest.setPhone("+9876543210");

    changePasswordRequest = new ChangePasswordRequest();
    changePasswordRequest.setCurrentPassword("password123");
    changePasswordRequest.setNewPassword("newpassword123");
  }

  @AfterEach
  void tearDown() {
    // Clear security context after each test
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Should get current user successfully")
  void shouldGetCurrentUserSuccessfully() {
    // Given
    when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(user));

    // When
    UserDTO result = userService.getCurrentUser();

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo(user.getUsername());
    assertThat(result.getFirstName()).isEqualTo(user.getFirstName());
    assertThat(result.getLastName()).isEqualTo(user.getLastName());
    assertThat(result.getEmail()).isEqualTo(user.getEmail());
    assertThat(result.getPhone()).isEqualTo(user.getPhone());

    verify(userRepository).findByUsernameIgnoreCase("testuser");
  }

  @Test
  @DisplayName("Should throw exception when user not found during getCurrentUser")
  void shouldThrowExceptionWhenUserNotFoundDuringGetCurrentUser() {
    // Given
    when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> userService.getCurrentUser())
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHENTICATION_CREDENTIALS_NOT_FOUND);

    verify(userRepository).findByUsernameIgnoreCase("testuser");
  }

  @Test
  @DisplayName("Should update current user successfully")
  void shouldUpdateCurrentUserSuccessfully() {
    // Given
    when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(user));
    // Only check existsByEmailIgnoreCase if email is different
    when(userRepository.save(user)).thenReturn(user);

    // When
    UserDTO result = userService.updateCurrentUser(updateUserRequest);

    // Then
    assertThat(result).isNotNull();
    verify(userRepository).findByUsernameIgnoreCase("testuser");
    verify(userRepository).save(user);

    // Verify user fields were updated
    assertThat(user.getFirstName()).isEqualTo(updateUserRequest.getFirstName());
    assertThat(user.getLastName()).isEqualTo(updateUserRequest.getLastName());
    assertThat(user.getEmail()).isEqualTo(updateUserRequest.getEmail());
    assertThat(user.getPhone()).isEqualTo(updateUserRequest.getPhone());
  }


  @Test
  @DisplayName("Should throw exception when user not found during updateCurrentUser")
  void shouldThrowExceptionWhenUserNotFoundDuringUpdateCurrentUser() {
    // Given
    when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> userService.updateCurrentUser(updateUserRequest))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTHENTICATION_CREDENTIALS_NOT_FOUND);

    verify(userRepository).findByUsernameIgnoreCase("testuser");
    verify(userRepository, never()).existsByEmailIgnoreCase(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should change password successfully")
  void shouldChangePasswordSuccessfully() {
    // Given
    when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(),
                                 "encodedPassword")).thenReturn(true);
    when(passwordEncoder.encode(changePasswordRequest.getNewPassword())).thenReturn(
        "newEncodedPassword");

    // When
    userService.changePassword(changePasswordRequest);

    // Then
    verify(userRepository).findByUsernameIgnoreCase("testuser");
    verify(passwordEncoder).matches(changePasswordRequest.getCurrentPassword(), "encodedPassword");
    verify(passwordEncoder).encode(changePasswordRequest.getNewPassword());
    // capture the updated user
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());

    // Retrieve the captured parameter
    User updatedUser = captor.getValue();

    // Verify password was updated
    assertThat(updatedUser.getPassword()).isEqualTo("newEncodedPassword");
  }

  @Test
  @DisplayName("Should throw exception when current password is invalid during changePassword")
  void shouldThrowExceptionWhenCurrentPasswordIsInvalidDuringChangePassword() {
    // Given
    when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(),
                                 user.getPassword())).thenReturn(false);

    // When/Then
    assertThatThrownBy(() -> userService.changePassword(changePasswordRequest))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_CREDENTIALS);

    verify(userRepository).findByUsernameIgnoreCase("testuser");
    verify(passwordEncoder).matches(changePasswordRequest.getCurrentPassword(), user.getPassword());
    verify(passwordEncoder, never()).encode(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should do nothing when user not found during changePassword")
  void shouldDoNothingWhenUserNotFoundDuringChangePassword() {
    // Given
    when(userRepository.findByUsernameIgnoreCase("testuser")).thenReturn(Optional.empty());

    // When
    userService.changePassword(changePasswordRequest);

    // Then
    verify(userRepository).findByUsernameIgnoreCase("testuser");
    verify(passwordEncoder, never()).matches(any(), any());
    verify(passwordEncoder, never()).encode(any());
    verify(userRepository, never()).save(any());
  }
}
