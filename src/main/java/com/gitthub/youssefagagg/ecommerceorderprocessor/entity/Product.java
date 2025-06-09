package com.gitthub.youssefagagg.ecommerceorderprocessor.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Product entity for storing product information.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public class Product extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(min = 3,
        max = 100)
  @Column(name = "name",
          length = 100,
          nullable = false)
  private String name;

  @Size(max = 1000)
  @Column(name = "description",
          length = 1000)
  private String description;

  @NotNull
  @DecimalMin(value = "0.0",
              inclusive = false)
  @Column(name = "price",
          nullable = false,
          precision = 10,
          scale = 2)
  private BigDecimal price;

  @OneToOne(mappedBy = "product")
  @ToString.Exclude
  @JsonIgnore
  private Inventory inventory;

  public Product cloneObject() {
    return Product.builder()
                  .id(this.id)
                  .name(this.name)
                  .description(this.description)
                  .price(this.price)
                  .createdBy(this.getCreatedBy())
                  .createdDate(this.getCreatedDate())
                  .lastModifiedBy(this.getLastModifiedBy())
                  .lastModifiedDate(this.getLastModifiedDate())
                  .build();
  }
}