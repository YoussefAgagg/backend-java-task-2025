package com.gitthub.youssefagagg.ecommerceorderprocessor.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for refreshing access token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for obtaining a new access token using a refresh token")
public class RefreshTokenRequest {

  @NotBlank
  @Schema(description = "Valid refresh token previously issued during authentication",
          example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyLCJ0eXBlIjoicmVmcmVzaCJ9.oUZe4fRYrTMTKr_KA0zXJwbdmcEGIwTgYQRKkwGnWFE",
          required = true)
  private String refreshToken;

}