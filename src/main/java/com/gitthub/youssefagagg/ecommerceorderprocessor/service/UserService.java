package com.gitthub.youssefagagg.ecommerceorderprocessor.service;


import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.ChangePasswordRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.UpdateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.UserDTO;

/**
 * Service Interface for user self-management operations.
 */
public interface UserService {

  /**
   * Updates the current user's profile.
   *
   * @param updateUserRequest the entity to update
   * @return the persisted entity
   */
  UserDTO updateCurrentUser(UpdateUserRequest updateUserRequest);

  /**
   * Get the current user.
   *
   * @return the entity
   */
  UserDTO getCurrentUser();



  /**
   * Change the current user's password.
   *
   * @param changePasswordRequest the password change information
   */
  void changePassword(ChangePasswordRequest changePasswordRequest);

}
