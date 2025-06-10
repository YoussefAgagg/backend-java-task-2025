package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * DTO for user login.
 */
@Data
@ToString(exclude = {"password"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data transfer object for authenticating user credentials")
public class LoginRequest {

  @NotBlank
  @Size(min = 3,
        max = 50)
  @Pattern(regexp = "^[a-zA-Z0-9_-]{3,50}$",
           message = "Username must be 3-50 characters long and can contain letters, numbers, underscores, and hyphens")
  @Schema(description = "Username or email used for authentication",
          example = "john_doe",
          minLength = 3,
          maxLength = 50,
          pattern = "^[a-zA-Z0-9_-]{3,50}$")
  private String username;

  @NotBlank
  @Size(min = 4,
        max = 100)
  @Schema(description = "User's password",
          minLength = 4,
          maxLength = 100)
  private String password;

}