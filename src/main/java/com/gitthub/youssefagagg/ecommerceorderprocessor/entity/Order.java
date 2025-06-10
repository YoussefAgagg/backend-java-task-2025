package com.gitthub.youssefagagg.ecommerceorderprocessor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Order entity for storing order information.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"user", "orderItems"})
public class Order extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id",
              nullable = false)
  private User user;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status",
          nullable = false)
  private OrderStatus status;

  @NotNull
  @DecimalMin(value = "0.0",
              inclusive = true)
  @Column(name = "total_amount",
          nullable = false,
          precision = 10,
          scale = 2)
  private BigDecimal totalAmount;

  @OneToMany(mappedBy = "order",
             fetch = FetchType.EAGER)
  @Builder.Default
  private List<OrderItem> orderItems = new ArrayList<>();

  /**
   * Idempotency key to prevent duplicate order processing. This should be a unique value generated
   * by the client for each order attempt.
   */
  @Column(name = "idempotency_key",
          length = 100)
  private String idempotencyKey;

  /**
   * Update order status and trigger appropriate actions.
   *
   * @param newStatus the new status
   */
  public void updateStatus(OrderStatus newStatus) {
    // Don't allow status changes from CANCELLED
    if (this.status == OrderStatus.CANCELLED) {
      return;
    }

    // Don't allow going back to earlier statuses
    if (isStatusRegression(newStatus)) {
      return;
    }

    this.status = newStatus;
  }

  private boolean isStatusRegression(OrderStatus newStatus) {
    // Define the order of statuses
    List<OrderStatus> statusOrder = List.of(
        OrderStatus.PENDING,
        OrderStatus.PAID,
        OrderStatus.PROCESSING,
        OrderStatus.SHIPPED,
        OrderStatus.DELIVERED
                                           );

    int currentIndex = statusOrder.indexOf(this.status);
    int newIndex = statusOrder.indexOf(newStatus);

    // If either status is not in the list or new status comes before current
    return currentIndex == -1 || newIndex == -1 || newIndex < currentIndex;
  }

  public Order cloneObject() {
    return Order.builder()
                .id(this.id)
                .user(this.user)
                .status(this.status)
                .totalAmount(this.totalAmount)
                .orderItems(new ArrayList<>(this.orderItems))
                .idempotencyKey(this.idempotencyKey)
                .createdBy(this.getCreatedBy())
                .createdDate(this.getCreatedDate())
                .lastModifiedBy(this.getLastModifiedBy())
                .lastModifiedDate(this.getLastModifiedDate())
                .build();
  }
}
