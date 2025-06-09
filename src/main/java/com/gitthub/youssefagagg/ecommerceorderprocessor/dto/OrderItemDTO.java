package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for {@link OrderItem}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderItemDTO extends AbstractAuditingEntityDto {

  private Long id;

  @NotNull
  private Long orderId;

  @NotNull
  private Long productId;

  private String productName;

  @NotNull
  @Min(1)
  private Integer quantity;

  @NotNull
  @DecimalMin(value = "0.0",
              inclusive = false)
  private BigDecimal price;

  private BigDecimal subtotal;
}