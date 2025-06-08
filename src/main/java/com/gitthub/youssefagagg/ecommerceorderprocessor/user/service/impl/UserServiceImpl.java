package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.SecurityUtils;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.ChangePasswordRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UpdateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UserDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.mapper.UserMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.UserService;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for user self-management operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final Random RANDOM = new Random();
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserDTO updateCurrentUser(UpdateUserRequest updateUserRequest) {
    log.debug("Request to update current user : {}", updateUserRequest);

    return SecurityUtils.getCurrentUserUserName()
                        .flatMap(userRepository::findByUsernameIgnoreCase)
                        .map(currentUser -> {
                          // Ensure the user can only update their own profile
                          updateUserRequest.setId(currentUser.getId());

                          // Check if email is being changed and already exists
                          if (updateUserRequest.getEmail() != null &&
                              !updateUserRequest.getEmail().equals(currentUser.getEmail()) &&
                              userRepository.existsByEmailIgnoreCase(
                                  updateUserRequest.getEmail())) {
                            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS,
                                                      "Email already exists: "
                                                      + updateUserRequest.getEmail());
                          }

                          // Update user fields - username is not updated
                          currentUser.setFirstName(updateUserRequest.getFirstName());
                          currentUser.setLastName(updateUserRequest.getLastName());
                          currentUser.setEmail(updateUserRequest.getEmail());
                          currentUser.setPhone(updateUserRequest.getPhone());
                          return userMapper.toDto(userRepository.save(currentUser));
                        })
                        .orElseThrow(() -> new CustomException(
                            ErrorCode.AUTHENTICATION_CREDENTIALS_NOT_FOUND,
                            "User not authenticated"));
  }

  @Override
  @Transactional(readOnly = true)
  public UserDTO getCurrentUser() {
    log.debug("Request to get current user");
    return SecurityUtils.getCurrentUserUserName()
                        .flatMap(userRepository::findByUsernameIgnoreCase)
                        .map(userMapper::toDto)
                        .orElseThrow(() -> new CustomException(
                            ErrorCode.AUTHENTICATION_CREDENTIALS_NOT_FOUND,
                            "User not authenticated"));
  }


  @Override
  @Transactional
  public void changePassword(ChangePasswordRequest changePasswordRequest) {
    log.debug("Request to change password for current user");
    SecurityUtils.getCurrentUserUserName()
                 .flatMap(userRepository::findByUsernameIgnoreCase)
                 .ifPresent(user -> {
                   if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(),
                                                user.getPassword())) {
                     throw new CustomException(ErrorCode.BAD_CREDENTIALS,
                                               "Invalid current password");
                   }
                   user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                   userRepository.save(user);
                   log.debug("Changed password for User: {}", user);
                 });
  }

}
