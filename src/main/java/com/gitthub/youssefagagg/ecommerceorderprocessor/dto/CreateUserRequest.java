package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
 * DTO for creating a new user.
 */
@Getter
@Setter
@ToString(exclude = {"password"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data transfer object for creating a new user account")
public class CreateUserRequest {


  @NotNull
  @Size(max = 50)
  @Schema(description = "User's first name",
          example = "John",
          maxLength = 50)
  private String firstName;
  @NotBlank
  @Size(min = 3,
        max = 50)
  @Pattern(regexp = "^[a-zA-Z0-9_-]{3,50}$",
           message = "Username must be 3-50 characters long and can contain letters, numbers, underscores, and hyphens")
  @Schema(description = "Username for the new account",
          example = "john_doe",
          minLength = 3,
          maxLength = 50,
          pattern = "^[a-zA-Z0-9_-]{3,50}$")
  private String username;

  @NotNull
  @Size(max = 50)
  @Schema(description = "User's last name",
          example = "Doe",
          maxLength = 50)
  private String lastName;

  @Email
  @Size(min = 5,
        max = 254)
  @Schema(description = "User's email address",
          example = "john.doe@example.com",
          minLength = 5,
          maxLength = 254)
  private String email;

  @NotBlank
  @Pattern(regexp = "^\\+?[0-9]{10,15}$")
  @Schema(description = "User's phone number (10-15 digits, can start with '+')",
          example = "+201234567890",
          pattern = "^\\+?[0-9]{10,15}$")
  private String phone;


  @NotBlank
  @Size(min = 4,
        max = 100)
  @Schema(description = "Password for the new account",
          minLength = 4,
          maxLength = 100)
  private String password;
}
