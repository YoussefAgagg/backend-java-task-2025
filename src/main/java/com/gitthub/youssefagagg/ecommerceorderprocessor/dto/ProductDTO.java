package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for {@link Product}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductDTO {

  private Long id;

  @NotNull
  @Size(min = 3,
        max = 100)
  private String name;

  @Size(max = 1000)
  private String description;

  @NotNull
  @DecimalMin(value = "0.0",
              inclusive = false)
  private BigDecimal price;

  // Additional field to show inventory status
  private Integer availableQuantity;
}
