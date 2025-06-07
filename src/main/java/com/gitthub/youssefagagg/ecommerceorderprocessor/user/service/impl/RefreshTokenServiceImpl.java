package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.config.JwtProperties;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.security.jwt.TokenProvider;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.LoginRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.RefreshToken;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.repository.RefreshTokenRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing refresh tokens.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenProvider tokenProvider;
  private final JwtProperties jwtProperties;


  // No expiration for refresh tokens as per requirement
  @Override
  @Transactional
  public RefreshToken createRefreshToken(User user,
                                         LoginRequest loginRequest,
                                         HttpServletRequest request) {
    String refreshTokenString = tokenProvider.createRefreshToken(user.getUsername());
    RefreshToken refreshToken = RefreshToken.builder()
                                            .user(user)
                                            .token(refreshTokenString)
                                            .expiryDate(Instant.now().plusSeconds(
                                                jwtProperties.getRefreshTokenExpiration()))
                                            .issuedDate(Instant.now())
                                            .lastLoginDate(Instant.now()) // Set last login date
                                            .revoked(false)
                                            .deviceInfo(loginRequest.getDeviceInfo())
                                            .deviceInfo(loginRequest.getDeviceInfo() != null
                                                        ? loginRequest.getDeviceInfo() : "Unknown")
                                            .userAgent(request.getHeader("User-Agent"))
                                            .ipAddress(getClientIp(request))
                                            .build();

    return refreshTokenRepository.save(refreshToken);
  }

  private String getClientIp(HttpServletRequest request) {
    String ipAddress = request.getHeader(
        "X-Forwarded-For"); // Check if request passed through a proxy
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("X-Real-IP"); // Alternative header
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr(); // Fallback to the remote address directly
    }

    // If there are multiple IPs in X-Forwarded-For, take the first one (the actual client IP)
    if (ipAddress != null && ipAddress.contains(",")) {
      ipAddress = ipAddress.split(",")[0].trim();
    }
    return ipAddress;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByTokenAndRevokedIsFalse(token);
  }

  @Override
  @Transactional
  public RefreshToken verifyExpiration(RefreshToken token) {
    // No expiration check as per requirement
    // Only check if the token is revoked
    if (token.isRevoked()) {
      throw new CustomException(ErrorCode.AUTHENTICATION_CREDENTIALS_NOT_FOUND,
                                "Refresh token was revoked. Please make a new login request");
    }

    // Update last login date
    token.setLastLoginDate(Instant.now());
    return refreshTokenRepository.save(token);
  }

  @Override
  @Transactional
  public void revokeRefreshToken(String token) {
    refreshTokenRepository.findByTokenAndRevokedIsFalse(token)
                          .ifPresentOrElse(refreshToken -> {
                            refreshToken.setRevoked(true);
                            refreshTokenRepository.save(refreshToken);
                          }, () -> {
                            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                                                      "Refresh token not found");
                          });
  }

  @Override
  @Transactional
  public void revokeAllUserTokens(User user) {
    refreshTokenRepository.updateRevokedByUser(user, true);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RefreshToken> findAllByUser(User user) {
    return refreshTokenRepository.findAllByUser(user);
  }

  @Override
  @Transactional
  // Run at midnight every day
  public void deleteExpiredTokens() {
    // Since refresh tokens don't expire anymore, this method is kept for interface compatibility
    // but doesn't perform any deletion based on expiry date
    log.info("Refresh tokens do not expire as per requirement. No tokens deleted.");
  }
}
