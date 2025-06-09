package com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository;

import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.ProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Product;
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
   * Find all products with their inventory information.
   *
   * @param pageable the pagination information
   * @return a page of ProductDTO containing product details and inventory status
   */
  @Query("""
                 SELECT new com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.ProductDTO(
                     p.id, p.name, p.description, p.price, i.quantity
                 )
                 FROM Product p
                 LEFT JOIN Inventory i ON p.id = i.product.id
         """)
  Page<ProductDTO> findAllProductWithInventory(Pageable pageable);

  /**
   * Find a product by its ID with inventory information.
   *
   * @param id the product ID
   * @return an Optional containing the ProductDTO if found, empty otherwise
   */
  @Query("""
                 SELECT new com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.ProductDTO(
                     p.id, p.name, p.description, p.price, i.quantity
                 )
                 FROM Product p
                 LEFT JOIN Inventory i ON p.id = i.product.id
                 WHERE p.id = :id
         """)
  Optional<ProductDTO> findProductWithInventoryById(Long id);

  /**
   * Find products by IDs with their inventory loaded.
   *
   * @param productIds the list of product IDs
   * @return the list of products with their inventory loaded
   */
  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.inventory WHERE p.id IN :productIds")
  List<Product> findByIdInWithInventory(@Param("productIds") List<Long> productIds);
}
