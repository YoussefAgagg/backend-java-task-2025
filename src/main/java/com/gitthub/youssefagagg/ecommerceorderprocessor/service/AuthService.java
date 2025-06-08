package com.gitthub.youssefagagg.ecommerceorderprocessor.service;


import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.CreateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.LoginRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.TokenDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.UserDTO;

/**
 * Service Interface for managing authentication and authorization.
 */
public interface AuthService {

  /**
   * Register a new user.
   *
   * @param createUserRequest the registration information
   * @return the registered user
   */
  UserDTO register(CreateUserRequest createUserRequest);

  /**
   * Authenticate a user.
   *
   * @param loginRequest the login information
   * @return the authentication token
   */
  TokenDTO login(LoginRequest loginRequest);

}
