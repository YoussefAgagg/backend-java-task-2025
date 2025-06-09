package com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.AbstractAuditingEntityDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for {@link com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Inventory}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class InventoryDTO extends AbstractAuditingEntityDto {

  private Long id;

  @NotNull
  private Long productId;

  private String productName;

  @NotNull
  @Min(0)
  private Integer quantity;

  @NotNull
  @Min(0)
  private Integer reservedQuantity;

  private Integer availableQuantity;
}