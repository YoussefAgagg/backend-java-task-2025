package com.gitthub.youssefagagg.ecommerceorderprocessor.user.web.rest.v1;

import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.ChangePasswordRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UpdateUserRequest;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto.UserDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User - User-Profile")
public class UserController {

  private final UserService userService;

  /**
   * {@code GET  /me} : Get the current user.
   *
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the current user
   */
  @GetMapping("/me")
  public ResponseEntity<UserDTO> getCurrentUser() {
    log.debug("REST request to get current user");
    return ResponseEntity.ok().body(userService.getCurrentUser());
  }

  /**
   * {@code PUT  /me} : Update the current user.
   *
   * @param updateUserRequest the user to update
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user
   */
  @PutMapping("/me")
  public ResponseEntity<UserDTO> updateCurrentUser(
      @Valid @RequestBody UpdateUserRequest updateUserRequest) {
    log.debug("REST request to update current user : {}", updateUserRequest);
    UserDTO updatedUser = userService.updateCurrentUser(updateUserRequest);
    return ResponseEntity.ok().body(updatedUser);
  }

  /**
   * {@code POST  /change-password} : Change the current user's password.
   *
   * @param changePasswordRequest the password change information
   * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}
   */
  @PostMapping("/change-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
    log.debug("REST request to change password for current user");
    userService.changePassword(changePasswordRequest);
    return ResponseEntity.noContent().build();
  }
}
