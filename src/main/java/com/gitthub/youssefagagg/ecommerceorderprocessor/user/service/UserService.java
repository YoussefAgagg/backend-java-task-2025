package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service;


import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.ChangePasswordRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UpdateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UserDTO;

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
   * Soft delete the current user's account.
   */
  void deleteCurrentUser();


  /**
   * Change the current user's password.
   *
   * @param changePasswordRequest the password change information
   */
  void changePassword(ChangePasswordRequest changePasswordRequest);

}
