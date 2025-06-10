package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for {@link OrderItem}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderItemDTO {

  private Long id;

  private Long orderId;

  @NotNull
  private Long productId;

  private String productName;

  @NotNull
  @Min(1)
  private Integer quantity;


  private BigDecimal price;

  private BigDecimal subtotal;
}