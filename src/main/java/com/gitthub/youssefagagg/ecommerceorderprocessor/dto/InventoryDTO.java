package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Inventory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for {@link Inventory}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InventoryDTO {

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