package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.AuditLog;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for {@link AuditLog}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AuditLogDTO extends AbstractAuditingEntityDto {

  private Long id;

  @NotNull
  @Size(max = 50)
  private String entityType;

  @NotNull
  private Long entityId;

  @NotNull
  @Size(max = 50)
  private String action;

  private String changes;
}