package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.CreateProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.InventoryDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.ProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Inventory;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.InventoryMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.ProductMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.InventoryRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.ProductRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.AuditService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.WebSocketService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.impl.ProductServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);
  private final InventoryMapper inventoryMapper = Mappers.getMapper(InventoryMapper.class);

  @Mock
  private ProductRepository productRepository;
  @Mock
  private InventoryRepository inventoryRepository;
  @Mock
  private UserRepository userRepository;

  @Mock
  private WebSocketService webSocketService;
  @Mock
  private AuditService auditService;

  private ProductServiceImpl productService;

  private Product product;
  private ProductDTO productDTO;
  private CreateProductDTO createProductDTO;
  private Inventory inventory;
  private InventoryDTO inventoryDTO;

  @BeforeEach
  void setUp() {
    productService = new ProductServiceImpl(
        userRepository,
        productRepository,
        inventoryRepository,
        productMapper,
        inventoryMapper,
        webSocketService,
        auditService
    );

    // Setup test data
    product = new Product();
    product.setId(1L);
    product.setName("Test Product");
    product.setDescription("Test Description");
    product.setPrice(BigDecimal.valueOf(99.99));

    inventory = new Inventory();
    inventory.setId(1L);
    inventory.setProduct(product);
    inventory.setQuantity(10);
    inventory.setReservedQuantity(2);

    product.setInventory(inventory);

    productDTO = new ProductDTO();
    productDTO.setId(1L);
    productDTO.setName("Test Product");
    productDTO.setDescription("Test Description");
    productDTO.setPrice(BigDecimal.valueOf(99.99));
    productDTO.setAvailableQuantity(8); // 10 - 2

    createProductDTO = new CreateProductDTO();
    createProductDTO.setId(1L);
    createProductDTO.setName("Test Product");
    createProductDTO.setDescription("Test Description");
    createProductDTO.setPrice(BigDecimal.valueOf(99.99));
    createProductDTO.setQuantity(10);

    inventoryDTO = new InventoryDTO();
    inventoryDTO.setId(1L);
    inventoryDTO.setProductId(1L);
    inventoryDTO.setProductName("Test Product");
    inventoryDTO.setQuantity(10);
    inventoryDTO.setReservedQuantity(2);
    inventoryDTO.setAvailableQuantity(8);
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

    Product newProduct = new Product();
    newProduct.setId(2L);
    newProduct.setName("New Product");
    newProduct.setDescription("New Description");
    newProduct.setPrice(BigDecimal.valueOf(49.99));

    when(productRepository.save(any(Product.class))).thenReturn(newProduct);

    // When
    ProductDTO result = productService.save(newProductDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(newProductDTO.getName());
    assertThat(result.getDescription()).isEqualTo(newProductDTO.getDescription());
    assertThat(result.getPrice()).isEqualTo(newProductDTO.getPrice());
    assertThat(result.getAvailableQuantity()).isEqualTo(newProductDTO.getQuantity());

    verify(productRepository).save(any(Product.class));
    verify(inventoryRepository).save(any(Inventory.class));
    verify(auditService).createLogAsync("Product", newProduct.getId(), newProduct);
    verify(auditService).createLogAsync(any(), any(), any(Inventory.class));
  }

  @Test
  @DisplayName("Should update product successfully")
  void shouldUpdateProductSuccessfully() {
    // Given
    CreateProductDTO updateProductDTO = new CreateProductDTO();
    updateProductDTO.setId(1L);
    updateProductDTO.setName("Updated Product");
    updateProductDTO.setDescription("Updated Description");
    updateProductDTO.setPrice(BigDecimal.valueOf(149.99));
    updateProductDTO.setQuantity(15);

    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(product)).thenReturn(product);
    when(inventoryRepository.save(inventory)).thenReturn(inventory);

    // When
    ProductDTO result = productService.update(updateProductDTO);

    // Then
    assertThat(result).isNotNull();
    verify(productRepository).findById(1L);
    verify(productRepository).save(product);
    verify(inventoryRepository).save(inventory);
    // Verify audit logs for both product and inventory updates
    verify(auditService, times(2)).updateLogAsync(any(), anyLong(), any(), any(), any());
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent product")
  void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
    // Given
    CreateProductDTO updateProductDTO = new CreateProductDTO();
    updateProductDTO.setId(999L);
    updateProductDTO.setName("Non-existent Product");
    updateProductDTO.setQuantity(5);

    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> productService.update(updateProductDTO))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(productRepository).findById(999L);
    verify(productRepository, never()).save(any());
    verify(inventoryRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when updating product with null ID")
  void shouldThrowExceptionWhenUpdatingProductWithNullId() {
    // Given
    CreateProductDTO updateProductDTO = new CreateProductDTO();
    updateProductDTO.setName("Invalid Product");
    updateProductDTO.setQuantity(5);

    // When/Then
    assertThatThrownBy(() -> productService.update(updateProductDTO))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

    verify(productRepository, never()).findById(any());
    verify(productRepository, never()).save(any());
    verify(inventoryRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should find one product by ID")
  void shouldFindOneProductById() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    // When
    ProductDTO result = productService.findOne(1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(product.getId());
    assertThat(result.getName()).isEqualTo(product.getName());
    assertThat(result.getDescription()).isEqualTo(product.getDescription());
    assertThat(result.getPrice()).isEqualTo(product.getPrice());
    assertThat(result.getAvailableQuantity()).isEqualTo(
        product.getInventory().getAvailableQuantity());

    verify(productRepository).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when finding non-existent product")
  void shouldThrowExceptionWhenFindingNonExistentProduct() {
    // Given
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> productService.findOne(999L))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(productRepository).findById(999L);
  }

  @Test
  @DisplayName("Should find all products with pagination")
  void shouldFindAllProductsWithPagination() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    List<Product> products = new ArrayList<>();
    products.add(product);
    Page<Product> page = new PageImpl<>(products, pageable, products.size());

    when(productRepository.findAll(pageable)).thenReturn(page);

    // When
    PaginationResponse<ProductDTO> result = productService.findAll(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(product.getId());
    assertThat(result.data().get(0).getName()).isEqualTo(product.getName());
    assertThat(result.data().get(0).getAvailableQuantity()).isEqualTo(
        product.getInventory().getAvailableQuantity());
    assertThat(result.totalCount()).isEqualTo(1);

    verify(productRepository).findAll(pageable);
  }

  @Test
  @DisplayName("Should find products by name containing with pagination")
  void shouldFindProductsByNameContainingWithPagination() {
    // Given
    String searchName = "Test";
    Pageable pageable = PageRequest.of(0, 10);
    List<Product> products = new ArrayList<>();
    products.add(product);
    Page<Product> page = new PageImpl<>(products, pageable, products.size());

    when(productRepository.findByNameContainingIgnoreCase(searchName, pageable)).thenReturn(page);

    // When
    PaginationResponse<ProductDTO> result = productService.findByNameContaining(searchName,
                                                                                pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(product.getId());
    assertThat(result.data().get(0).getName()).isEqualTo(product.getName());
    assertThat(result.data().get(0).getAvailableQuantity()).isEqualTo(
        product.getInventory().getAvailableQuantity());
    assertThat(result.totalCount()).isEqualTo(1);

    verify(productRepository).findByNameContainingIgnoreCase(searchName, pageable);
  }

  @Test
  @DisplayName("Should delete product successfully")
  void shouldDeleteProductSuccessfully() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    inventory.setQuantity(0); // Set quantity to 0 to allow deletion

    // When
    productService.delete(1L);

    // Then
    verify(productRepository).findById(1L);
    verify(inventoryRepository).delete(inventory);
    verify(productRepository).deleteById(1L);
    verify(auditService).deleteLogAsync("Product", 1L);
    verify(auditService).deleteLogAsync("Inventory", inventory.getId());
  }

  @Test
  @DisplayName("Should throw exception when deleting product with inventory")
  void shouldThrowExceptionWhenDeletingProductWithInventory() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    // inventory.quantity is already set to 10 in setUp()

    // When/Then
    assertThatThrownBy(() -> productService.delete(1L))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OPTIMISTIC_LOCKING_ERROR);

    verify(productRepository).findById(1L);
    verify(inventoryRepository, never()).delete(any(Inventory.class));
    verify(productRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent product")
  void shouldThrowExceptionWhenDeletingNonExistentProduct() {
    // Given
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> productService.delete(999L))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(productRepository).findById(999L);
    verify(inventoryRepository, never()).delete(any(Inventory.class));
    verify(productRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("Should get inventory for a product")
  void shouldGetInventoryForProduct() {
    // Given
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    // When
    InventoryDTO result = productService.getInventory(1L);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(inventory.getId());
    assertThat(result.getProductId()).isEqualTo(product.getId());
    assertThat(result.getQuantity()).isEqualTo(inventory.getQuantity());
    assertThat(result.getReservedQuantity()).isEqualTo(inventory.getReservedQuantity());
    assertThat(result.getAvailableQuantity()).isEqualTo(inventory.getAvailableQuantity());

    verify(productRepository).findById(1L);
  }

  @Test
  @DisplayName("Should throw exception when getting inventory for non-existent product")
  void shouldThrowExceptionWhenGettingInventoryForNonExistentProduct() {
    // Given
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> productService.getInventory(999L))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(productRepository).findById(999L);
  }

  @Test
  @DisplayName("Should update inventory successfully")
  void shouldUpdateInventorySuccessfully() {
    // Given
    InventoryDTO updateInventoryDTO = new InventoryDTO();
    updateInventoryDTO.setId(1L);
    updateInventoryDTO.setProductId(1L);
    updateInventoryDTO.setQuantity(20);
    updateInventoryDTO.setReservedQuantity(5);

    when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
    when(inventoryRepository.save(inventory)).thenReturn(inventory);

    // When
    InventoryDTO result = productService.updateInventory(updateInventoryDTO);

    // Then
    assertThat(result).isNotNull();
    verify(inventoryRepository).findById(1L);
    verify(inventoryRepository).save(inventory);
    verify(auditService).updateLogAsync(any(), anyLong(), any(), any(), any());
    verify(webSocketService).sendInventoryUpdate(any(InventoryDTO.class));
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent inventory")
  void shouldThrowExceptionWhenUpdatingNonExistentInventory() {
    // Given
    InventoryDTO updateInventoryDTO = new InventoryDTO();
    updateInventoryDTO.setId(999L);
    updateInventoryDTO.setProductId(1L);
    updateInventoryDTO.setQuantity(20);

    when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> productService.updateInventory(updateInventoryDTO))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(inventoryRepository).findById(999L);
    verify(inventoryRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw exception when updating inventory with null ID")
  void shouldThrowExceptionWhenUpdatingInventoryWithNullId() {
    // Given
    InventoryDTO updateInventoryDTO = new InventoryDTO();
    updateInventoryDTO.setProductId(1L);
    updateInventoryDTO.setQuantity(20);

    // When/Then
    assertThatThrownBy(() -> productService.updateInventory(updateInventoryDTO))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

    verify(inventoryRepository, never()).findById(any());
    verify(inventoryRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should get low stock alerts")
  void shouldGetLowStockAlerts() {
    // Given
    int threshold = 5;
    List<Inventory> lowStockInventories = new ArrayList<>();
    lowStockInventories.add(inventory);

    when(inventoryRepository.findLowStock(threshold)).thenReturn(lowStockInventories);

    // When
    List<InventoryDTO> result = productService.getLowStockAlerts(threshold);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(inventory.getId());
    assertThat(result.get(0).getProductId()).isEqualTo(product.getId());
    assertThat(result.get(0).getQuantity()).isEqualTo(inventory.getQuantity());
    assertThat(result.get(0).getReservedQuantity()).isEqualTo(inventory.getReservedQuantity());
    assertThat(result.get(0).getAvailableQuantity()).isEqualTo(inventory.getAvailableQuantity());

    verify(inventoryRepository).findLowStock(threshold);
  }
}
