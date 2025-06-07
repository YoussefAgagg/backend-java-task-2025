package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service;


import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.CreateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.LoginRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.RefreshTokenDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.RefreshTokenRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.TokenDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;

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
   * @param request
   * @return the authentication token
   */
  TokenDTO login(LoginRequest loginRequest, HttpServletRequest request);

  /**
   * Refresh an access token.
   *
   * @param refreshTokenRequest the refresh token
   * @return the new authentication token
   */
  RefreshTokenDTO refreshToken(RefreshTokenRequest refreshTokenRequest);


  /**
   * Logout a user.
   *
   * @param refreshTokenRequest the refresh token to invalidate
   */
  void logout(RefreshTokenRequest refreshTokenRequest);
}
