package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.gitthub.youssefagagg.ecommerceorderprocessor.TestcontainersConfiguration;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.CreateProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.InventoryDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.ProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Inventory;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.InventoryRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.ProductRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.ProductService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class ProductServiceIT {

  @Autowired
  private ProductService productService;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private InventoryRepository inventoryRepository;

  private CreateProductDTO createProductDTO;
  private ProductDTO savedProduct;

  @BeforeEach
  void setUp() {
    // Clean up any existing data
    inventoryRepository.deleteAll();
    productRepository.deleteAll();

    // Setup test data
    createProductDTO = new CreateProductDTO();
    createProductDTO.setName("Test Product");
    createProductDTO.setDescription("Test Description");
    createProductDTO.setPrice(BigDecimal.valueOf(99.99));
    createProductDTO.setQuantity(10);

    try {
      // Save a product for tests that need an existing product
      savedProduct = productService.save(createProductDTO);

      // Ensure the product and inventory are properly linked in the database
      Product product = productRepository.findById(savedProduct.getId()).orElseThrow();
      Inventory inventory = inventoryRepository.findByProduct(product).orElseThrow();

      // Verify the relationship is established
      if (product.getInventory() == null) {
        product.setInventory(inventory);
        productRepository.save(product);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  @DisplayName("Should save product successfully")
  void shouldSaveProductSuccessfully() {
    // Given
    CreateProductDTO newProductDTO = new CreateProductDTO();
    newProductDTO.setName("New Product");
    newProductDTO.setDescription("New Description");
    newProductDTO.setPrice(BigDecimal.valueOf(49.99));
    newProductDTO.setQuantity(5);

    // When
    ProductDTO result = productService.save(newProductDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getName()).isEqualTo(newProductDTO.getName());
    assertThat(result.getDescription()).isEqualTo(newProductDTO.getDescription());
    assertThat(result.getPrice()).isEqualTo(newProductDTO.getPrice());
    assertThat(result.getAvailableQuantity()).isEqualTo(newProductDTO.getQuantity());

    // Verify product is persisted
    Product savedProduct = productRepository.findById(result.getId()).orElseThrow();
    assertThat(savedProduct.getName()).isEqualTo(newProductDTO.getName());
  }

  @Test
  @DisplayName("Should update product successfully")
  void shouldUpdateProductSuccessfully() {
    // Given
    CreateProductDTO updateProductDTO = new CreateProductDTO();
    updateProductDTO.setId(savedProduct.getId());
    updateProductDTO.setName("Updated Product");
    updateProductDTO.setDescription("Updated Description");
    updateProductDTO.setPrice(BigDecimal.valueOf(149.99));
    updateProductDTO.setQuantity(15);

    // When
    ProductDTO result = productService.update(updateProductDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(savedProduct.getId());
    assertThat(result.getName()).isEqualTo(updateProductDTO.getName());
    assertThat(result.getDescription()).isEqualTo(updateProductDTO.getDescription());
    assertThat(result.getPrice()).isEqualTo(updateProductDTO.getPrice());
    assertThat(result.getAvailableQuantity()).isEqualTo(updateProductDTO.getQuantity());
  }

  @Test
  @DisplayName("Should find all products with pagination")
  void shouldFindAllProductsWithPagination() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);

    // When
    PaginationResponse<ProductDTO> result = productService.findAll(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).isNotEmpty();
    assertThat(result.data().get(0).getId()).isEqualTo(savedProduct.getId());
    assertThat(result.data().get(0).getName()).isEqualTo(savedProduct.getName());
  }

  @Test
  @DisplayName("Should find one product by ID")
  void shouldFindOneProductById() {
    // When
    ProductDTO result = productService.findOne(savedProduct.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(savedProduct.getId());
    assertThat(result.getName()).isEqualTo(savedProduct.getName());
  }

  @Test
  @DisplayName("Should get inventory for a product")
  void shouldGetInventoryForProduct() {
    // When
    InventoryDTO result = productService.getInventory(savedProduct.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getProductId()).isEqualTo(savedProduct.getId());
    assertThat(result.getQuantity()).isEqualTo(10);
    assertThat(result.getReservedQuantity()).isEqualTo(0);
    assertThat(result.getAvailableQuantity()).isEqualTo(10);
  }

  @Test
  @DisplayName("Should update inventory successfully")
  void shouldUpdateInventorySuccessfully() {
    // Given
    Inventory inventory = inventoryRepository.findByProduct(
        productRepository.findById(savedProduct.getId()).orElseThrow()
                                                           ).orElseThrow();

    InventoryDTO updateInventoryDTO = new InventoryDTO();
    updateInventoryDTO.setId(inventory.getId());
    updateInventoryDTO.setProductId(savedProduct.getId());
    updateInventoryDTO.setQuantity(20);
    updateInventoryDTO.setReservedQuantity(5);

    // When
    InventoryDTO result = productService.updateInventory(updateInventoryDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(inventory.getId());
    assertThat(result.getQuantity()).isEqualTo(20);
    assertThat(result.getReservedQuantity()).isEqualTo(5);
    assertThat(result.getAvailableQuantity()).isEqualTo(15);
  }
}
