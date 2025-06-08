package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring token information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object containing authentication tokens and user information")
public class TokenDTO {

  @Schema(description = "JWT access token used for authenticating API requests",
          example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
  private String accessToken;

  @Schema(description = "Type of authentication token",
          example = "Bearer",
          allowableValues = {"Bearer"})
  private String tokenType;

  @Schema(description = "Username of the authenticated user",
          example = "johndoe")
  private String username;

  @Schema(description = "Email address of the authenticated user",
          example = "john.doe@example.com")
  private String email;

  @Schema(description = "First name of the authenticated user",
          example = "John")
  private String firstName;

  @Schema(description = "Last name of the authenticated user",
          example = "Doe")
  private String lastName;

}