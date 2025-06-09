package com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository;

import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Inventory;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Inventory} entity.
 */
@Repository
public interface InventoryRepository
    extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

  /**
   * Find inventory by product.
   *
   * @param product the product
   * @return the inventory if found, empty otherwise
   */
  Optional<Inventory> findByProduct(Product product);

  /**
   * Find inventory by product ID.
   *
   * @param productId the product ID
   * @return the inventory if found, empty otherwise
   */
  Optional<Inventory> findByProductId(Long productId);

  /**
   * Find products with low stock (available quantity below threshold).
   *
   * @param threshold the threshold
   * @return the list of inventories with low stock
   */
  @Query("SELECT i FROM Inventory i WHERE (i.quantity - i.reservedQuantity) < :threshold")
  List<Inventory> findLowStock(int threshold);
}