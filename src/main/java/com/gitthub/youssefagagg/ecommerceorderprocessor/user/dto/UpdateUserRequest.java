package com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO for updating an existing user.
 */
@Getter
@Setter
@ToString()
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for updating existing user information")
public class UpdateUserRequest {

  @Schema(description = "User ID to be updated",
          example = "1001")
  private Long id;

  @NotNull
  @Size(max = 50)
  @Schema(description = "User's updated first name (up to 50 characters)",
          example = "John",
          maxLength = 50)
  private String firstName;

  @NotNull
  @Size(max = 50)
  @Schema(description = "User's updated last name (up to 50 characters)",
          example = "Doe",
          maxLength = 50)
  private String lastName;

  @Email
  @Size(min = 5,
        max = 254)
  @Schema(description = "User's updated email address (5-254 characters)",
          example = "john.doe@example.com",
          minLength = 5,
          maxLength = 254,
          format = "email")
  private String email;

  @NotNull
  @Pattern(regexp = "^\\+?[0-9]{10,15}$")
  @Schema(description = "User's updated phone number (10-15 digits, may include + prefix)",
          example = "+12025550179",
          pattern = "^\\+?[0-9]{10,15}$")
  private String phone;
}
