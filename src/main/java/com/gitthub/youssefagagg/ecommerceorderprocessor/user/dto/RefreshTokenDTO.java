package com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning a refreshed access token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data transfer object containing a refreshed access token and related information")
public class RefreshTokenDTO {

  /**
   * The new access token.
   */
  @Schema(description = "The new JWT access token that can be used to authenticate requests",
          example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
  private String accessToken;

  /**
   * The token type, typically "Bearer".
   */
  @Schema(description = "The type of authentication token, almost always 'Bearer'",
          example = "Bearer",
          allowableValues = {"Bearer"})
  private String tokenType;

  /**
   * The token expiration time in seconds.
   */
  @Schema(description = "The number of seconds until the access token expires",
          example = "3600")
  private long expiresIn;
}