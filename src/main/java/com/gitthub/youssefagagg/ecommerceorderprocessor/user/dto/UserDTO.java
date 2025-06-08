package com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring user data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data transfer object containing user information")
public class UserDTO {

  @Schema(description = "Unique identifier of the user",
          example = "1001")
  private Long id;

  @NotBlank
  @Size(min = 3,
        max = 50)
  @Schema(description = "Username for authentication (3-50 characters)",
          example = "johndoe",
          minLength = 3,
          maxLength = 50,
          required = true)
  private String username;

  @Size(max = 50)
  @Schema(description = "User's first name (up to 50 characters)",
          example = "John",
          maxLength = 50)
  private String firstName;

  @Size(max = 50)
  @Schema(description = "User's last name (up to 50 characters)",
          example = "Doe",
          maxLength = 50)
  private String lastName;

  @Email
  @Size(min = 5,
        max = 254)
  @Schema(description = "User's email address (5-254 characters)",
          example = "john.doe@example.com",
          minLength = 5,
          maxLength = 254,
          format = "email")
  private String email;

  @Pattern(regexp = "^\\+?[0-9]{10,15}$")
  @Schema(description = "User's phone number (10-15 digits, may include + prefix)",
          example = "+12025550179",
          pattern = "^\\+?[0-9]{10,15}$")
  private String phone;

  @Schema(description = "Flag indicating if the user account is activated",
          example = "true")
  private boolean activated;

  @Schema(description = "User's preferred language code",
          example = "en",
          allowableValues = {"en", "ar", "fr", "de", "es"})
  private String langKey;

  @Schema(description = "Set of roles assigned to the user",
          example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
  private Set<String> roles;


}