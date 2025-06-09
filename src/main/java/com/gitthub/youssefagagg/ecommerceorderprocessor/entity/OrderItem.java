package com.gitthub.youssefagagg.ecommerceorderprocessor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * OrderItem entity for storing order item information.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"order", "product"})
public class OrderItem extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "order_id",
              nullable = false)
  @JsonIgnore
  private Order order;

  @ManyToOne
  @JoinColumn(name = "product_id",
              nullable = false)
  @JsonIgnore
  private Product product;

  @NotNull
  @Min(1)
  @Column(name = "quantity",
          nullable = false)
  private Integer quantity;

  @NotNull
  @DecimalMin(value = "0.0",
              inclusive = false)
  @Column(name = "price",
          nullable = false,
          precision = 10,
          scale = 2)
  private BigDecimal price;

  /**
   * Calculate the subtotal for this order item.
   *
   * @return the subtotal
   */
  public BigDecimal getSubtotal() {
    return price.multiply(BigDecimal.valueOf(quantity));
  }
}