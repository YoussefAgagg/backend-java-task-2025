package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for {@link Order}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderDTO extends AbstractAuditingEntityDto {

  private Long id;

  private Long userId;

  private String userName;

  @NotNull
  private OrderStatus status;

  @NotNull
  @DecimalMin(value = "0.0",
              inclusive = true)
  private BigDecimal totalAmount;

  @Valid
  @NotEmpty
  private List<OrderItemDTO> orderItems = new ArrayList<>();

  /**
   * Idempotency key to prevent duplicate order processing. This should be a unique value generated
   * by the client for each order attempt.
   */
  @NotBlank
  private String idempotencyKey;
}
