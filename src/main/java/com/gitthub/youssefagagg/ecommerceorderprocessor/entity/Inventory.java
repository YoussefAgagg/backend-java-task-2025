package com.gitthub.youssefagagg.ecommerceorderprocessor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Inventory entity for tracking product stock levels.
 */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = "product")
public class Inventory extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "product_id",
              nullable = false)
  @JsonIgnore
  private Product product;

  @NotNull
  @Min(0)
  @Column(name = "quantity",
          nullable = false)
  private Integer quantity;

  @NotNull
  @Min(0)
  @Column(name = "reserved_quantity",
          nullable = false)
  private Integer reservedQuantity;

  /**
   * Get available quantity (total quantity minus reserved quantity).
   *
   * @return available quantity
   */
  public Integer getAvailableQuantity() {
    return quantity - reservedQuantity;
  }

  /**
   * Reserve a quantity of the product.
   *
   * @param amount the amount to reserve
   * @return true if reservation was successful, false otherwise
   */
  public boolean reserve(int amount) {
    if (getAvailableQuantity() >= amount) {
      reservedQuantity += amount;
      return true;
    }
    return false;
  }

  /**
   * Release a reserved quantity of the product.
   *
   * @param amount the amount to release
   */
  public void releaseReservation(int amount) {
    reservedQuantity = Math.max(0, reservedQuantity - amount);
  }

  /**
   * Fulfill an order by reducing the quantity and reserved quantity.
   *
   * @param amount the amount to fulfill
   * @return true if fulfillment was successful, false otherwise
   */
  public boolean fulfill(int amount) {
    if (reservedQuantity >= amount) {
      quantity -= amount;
      reservedQuantity -= amount;
      return true;
    }
    return false;
  }


  public Inventory cloneObject() {
    return Inventory.builder()
                    .id(this.id)
                    .product(this.product != null ? this.product.cloneObject() : null)
                    .quantity(this.quantity)
                    .reservedQuantity(this.reservedQuantity)
                    .createdBy(this.getCreatedBy())
                    .createdDate(this.getCreatedDate())
                    .lastModifiedBy(this.getLastModifiedBy())
                    .lastModifiedDate(this.getLastModifiedDate())
                    .build();
  }
}