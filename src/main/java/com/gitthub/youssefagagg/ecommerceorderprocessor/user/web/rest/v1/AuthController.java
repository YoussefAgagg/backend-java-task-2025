package com.gitthub.youssefagagg.ecommerceorderprocessor.user.web.rest.v1;

import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.CreateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.LoginRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.RefreshTokenDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.RefreshTokenRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.TokenDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UserDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing authentication and authorization.
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User - Auth")
public class AuthController {

  private final AuthService authService;


  /**
   * {@code POST  /register} : Register a new user.
   *
   * @param createUserRequest the user to register
   * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user
   */
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<UserDTO> register(@Valid @RequestBody CreateUserRequest createUserRequest) {
    log.debug("REST request to register user : {}", createUserRequest.getUsername());
    UserDTO result = authService.register(createUserRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  /**
   * {@code POST  /login} : Authenticate a user.
   *
   * @param loginRequest the login credentials
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the
   *     authentication token
   */
  @PostMapping("/login")
  public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginRequest loginRequest,
                                        HttpServletRequest request) {
    log.debug("REST request to authenticate user : {}", loginRequest.getUsername());
    TokenDTO result = authService.login(loginRequest, request);
    return ResponseEntity.ok(result);
  }

  /**
   * {@code POST  /refresh-token} : Refresh an access token.
   *
   * @param refreshTokenRequest the refresh token
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the new
   *     authentication token
   */
  @PostMapping("/refresh-token")
  public ResponseEntity<RefreshTokenDTO> refreshToken(
      @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
    log.debug("REST request to refresh token");
    RefreshTokenDTO result = authService.refreshToken(refreshTokenRequest);
    return ResponseEntity.ok(result);
  }

  /**
   * {@code POST  /logout} : Logout a user.
   *
   * @param refreshTokenRequest the refresh token to invalidate
   * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}
   */
  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
    log.debug("REST request to logout user");
    authService.logout(refreshTokenRequest);
    return ResponseEntity.noContent().build();
  }
}
