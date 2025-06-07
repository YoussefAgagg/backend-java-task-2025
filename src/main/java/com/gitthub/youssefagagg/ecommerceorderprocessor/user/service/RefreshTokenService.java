package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service;


import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.LoginRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.RefreshToken;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing refresh tokens.
 */
public interface RefreshTokenService {

  /**
   * Create a new refresh token.
   *
   * @param user         the user to create a token for
   * @param loginRequest the login request containing token information
   * @param request      the HTTP server request
   * @return the created refresh token
   */
  RefreshToken createRefreshToken(User user, LoginRequest loginRequest, HttpServletRequest request);

  /**
   * Find a refresh token by token value.
   *
   * @param token the token value
   * @return the refresh token
   */
  Optional<RefreshToken> findByToken(String token);

  /**
   * Verify if a refresh token is valid.
   *
   * @param token the token to verify
   * @return the refresh token if valid
   */
  RefreshToken verifyExpiration(RefreshToken token);

  /**
   * Revoke a refresh token.
   *
   * @param token the token to revoke
   */
  void revokeRefreshToken(String token);

  /**
   * Revoke all refresh tokens for a user.
   *
   * @param user the user
   */
  void revokeAllUserTokens(User user);

  /**
   * Find all refresh tokens for a user.
   *
   * @param user the user
   * @return the list of refresh tokens
   */
  List<RefreshToken> findAllByUser(User user);

  /**
   * Delete all expired tokens.
   */
  void deleteExpiredTokens();
}