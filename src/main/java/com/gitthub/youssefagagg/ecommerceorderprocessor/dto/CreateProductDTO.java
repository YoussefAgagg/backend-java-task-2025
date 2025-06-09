package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating
 * {@link Product} with inventory
 * information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductDTO {

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

  @NotNull
  @Min(0)
  private Integer quantity;
}