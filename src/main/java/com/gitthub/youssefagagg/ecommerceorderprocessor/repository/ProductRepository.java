package com.gitthub.youssefagagg.ecommerceorderprocessor.repository;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Product} entity.
 */
@Repository
public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

  /**
   * Find a product by name.
   *
   * @param name the name to search for
   * @return the product if found, empty otherwise
   */
  Optional<Product> findByNameIgnoreCase(String name);

  /**
   * Check if a product name exists.
   *
   * @param name the name to check
   * @return true if the name exists, false otherwise
   */
  boolean existsByNameIgnoreCase(String name);

  /**
   * Find products by name containing the given text.
   *
   * @param name     the name to search for
   * @param pageable the pagination information
   * @return the list of products
   */
  Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

  /**
   * Find products by IDs with their inventory loaded.
   *
   * @param productIds the list of product IDs
   * @return the list of products with their inventory loaded
   */
  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.inventory WHERE p.id IN :productIds")
  List<Product> findByIdInWithInventory(@Param("productIds") List<Long> productIds);
}
