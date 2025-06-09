package com.gitthub.youssefagagg.ecommerceorderprocessor.product.service;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.CreateProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.InventoryDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.ProductDTO;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Interface for managing
 * {@link com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Product}.
 */
public interface ProductService {

  /**
   * Save a product.
   *
   * @param createProductDTO the entity to save with inventory quantity
   * @return the persisted entity
   */
  ProductDTO save(CreateProductDTO createProductDTO);

  /**
   * Updates an existing product.
   *
   * @param createProductDTO the entity to update with inventory quantity
   * @return the persisted entity
   */
  ProductDTO update(CreateProductDTO createProductDTO);

  /**
   * Get all products with pagination.
   *
   * @param pageable the pagination information
   * @return the list of entities
   */
  PaginationResponse<ProductDTO> findAll(Pageable pageable);

  /**
   * Get all products containing the given name with pagination.
   *
   * @param name     the name to search for
   * @param pageable the pagination information
   * @return the list of entities
   */
  PaginationResponse<ProductDTO> findByNameContaining(String name, Pageable pageable);

  /**
   * Get the "id" product.
   *
   * @param id the id of the entity
   * @return the entity
   */
  ProductDTO findOne(Long id);

  /**
   * Delete the "id" product.
   *
   * @param id the id of the entity
   */
  void delete(Long id);

  /**
   * Get inventory for a product.
   *
   * @param productId the id of the product
   * @return the inventory
   */
  InventoryDTO getInventory(Long productId);

  /**
   * Update inventory for a product.
   *
   * @param inventoryDTO the inventory to update
   * @return the updated inventory
   */
  InventoryDTO updateInventory(InventoryDTO inventoryDTO);

  @Transactional(readOnly = true)
  List<InventoryDTO> getLowStockAlerts(int threshold);
}
