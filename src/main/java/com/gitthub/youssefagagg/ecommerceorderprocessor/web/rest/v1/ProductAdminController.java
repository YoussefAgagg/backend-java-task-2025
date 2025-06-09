package com.gitthub.youssefagagg.ecommerceorderprocessor.web.rest.v1;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.CreateProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.InventoryDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.ProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing admin operations on
 * {@link Product}.
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Product Admin Operations")
public class ProductAdminController {

  private final ProductService productService;

  /**
   * {@code POST  /} : Create a new product.
   *
   * @param createProductDTO the product to create with inventory quantity
   * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new
   *     product
   */
  @PostMapping
  @Operation(summary = "Create a new product")
  public ResponseEntity<ProductDTO> createProduct(
      @Valid @RequestBody CreateProductDTO createProductDTO) {
    log.debug("REST request to save Product : {}", createProductDTO);
    ProductDTO result = productService.save(createProductDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  /**
   * {@code PUT  /{id}} : Updates an existing product.
   *
   * @param id               the id of the product to update
   * @param createProductDTO the product to update with inventory quantity
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated
   *     product
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update an existing product")
  public ResponseEntity<ProductDTO> updateProduct(
      @PathVariable Long id,
      @Valid @RequestBody CreateProductDTO createProductDTO) {
    log.debug("REST request to update Product : {}, {}", id, createProductDTO);
    createProductDTO.setId(id);
    ProductDTO result = productService.update(createProductDTO);
    return ResponseEntity.ok().body(result);
  }

  /**
   * {@code DELETE  /{id}} : Delete the "id" product.
   *
   * @param id the id of the product to delete
   * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a product")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    log.debug("REST request to delete Product : {}", id);
    productService.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * {@code PUT  /{id}/inventory} : Update inventory for a product.
   *
   * @param id           the id of the product
   * @param inventoryDTO the inventory to update
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated
   *     inventory
   */
  @PutMapping("/{id}/inventory")
  @Operation(summary = "Update inventory for a product")
  public ResponseEntity<InventoryDTO> updateInventory(
      @PathVariable Long id,
      @Valid @RequestBody InventoryDTO inventoryDTO) {
    log.debug("REST request to update Inventory for Product : {}, {}", id, inventoryDTO);
    inventoryDTO.setProductId(id);
    InventoryDTO result = productService.updateInventory(inventoryDTO);
    return ResponseEntity.ok().body(result);
  }

  /**
   * {@code GET  /inventory/low-stock} : Get low stock alerts.
   *
   * @param threshold the threshold
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the low stock
   *     alerts
   */
  @GetMapping("/inventory/low-stock")
  @Operation(summary = "Get low stock alerts (admin only)")
  public ResponseEntity<List<InventoryDTO>> getLowStockAlerts(
      @RequestParam(defaultValue = "5") int threshold) {
    log.debug("REST request to get low stock alerts with threshold: {}", threshold);
    List<InventoryDTO> lowStock = productService.getLowStockAlerts(threshold);
    return ResponseEntity.ok().body(lowStock);
  }
}