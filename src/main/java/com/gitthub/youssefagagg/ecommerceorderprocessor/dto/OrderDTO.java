package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for {@link Order}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderDTO {

  private Long id;

  private Long userId;

  private String userName;

  private OrderStatus status;

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
