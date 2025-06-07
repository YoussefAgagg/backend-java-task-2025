package com.gitthub.youssefagagg.ecommerceorderprocessor.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gitthub.youssefagagg.ecommerceorderprocessor.common.entity.AbstractAuditingEntity;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for {@link AbstractAuditingEntity}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AbstractAuditingEntityDto implements Serializable {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String createdBy;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant createdDate;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String lastModifiedBy;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Instant lastModifiedDate;
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Integer version;
}