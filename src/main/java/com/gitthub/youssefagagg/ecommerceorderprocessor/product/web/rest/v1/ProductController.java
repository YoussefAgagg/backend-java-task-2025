package com.gitthub.youssefagagg.ecommerceorderprocessor.product.web.rest.v1;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.InventoryDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.ProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing
 * {@link com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Product}.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management")
public class ProductController {

  private final ProductService productService;

  /**
   * {@code GET  /} : Get all products.
   *
   * @param pageable the pagination information
   * @param name     optional name filter
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of products in
   *     body
   */
  @GetMapping
  @Operation(summary = "Get all products with pagination")
  public ResponseEntity<PaginationResponse<ProductDTO>> getAllProducts(
      Pageable pageable,
      @RequestParam(required = false) String name) {
    log.debug("REST request to get a page of Products");
    PaginationResponse<ProductDTO> page = name != null ?
                                          productService.findByNameContaining(name, pageable) :
                                          productService.findAll(pageable);
    return ResponseEntity.ok().body(page);
  }

  /**
   * {@code GET  /{id}} : Get the "id" product.
   *
   * @param id the id of the product to retrieve
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the product, or
   *     with status {@code 404 (Not Found)}
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get a product by ID")
  public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
    log.debug("REST request to get Product : {}", id);
    ProductDTO productDTO = productService.findOne(id);
    return ResponseEntity.ok().body(productDTO);
  }

  /**
   * {@code GET  /{id}/inventory} : Get inventory for a product.
   *
   * @param id the id of the product
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the inventory
   */
  @GetMapping("/{id}/inventory")
  @Operation(summary = "Get inventory for a product")
  public ResponseEntity<InventoryDTO> getInventory(@PathVariable Long id) {
    log.debug("REST request to get Inventory for Product : {}", id);
    InventoryDTO inventoryDTO = productService.getInventory(id);
    return ResponseEntity.ok().body(inventoryDTO);
  }
}
