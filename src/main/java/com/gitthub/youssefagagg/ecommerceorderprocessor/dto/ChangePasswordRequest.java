package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for changing a user's password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data transfer object for changing a user's password")
public class ChangePasswordRequest {

  @NotBlank
  @Schema(description = "User's current password for verification",
          required = true)
  private String currentPassword;

  @NotBlank
  @Size(min = 4,
        max = 100)
  @Schema(description = "New password that will replace the current one",
          minLength = 4,
          maxLength = 100,
          required = true)
  private String newPassword;
}