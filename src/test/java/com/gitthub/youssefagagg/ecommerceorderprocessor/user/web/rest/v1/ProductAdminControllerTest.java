package com.gitthub.youssefagagg.ecommerceorderprocessor.user.web.rest.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.CreateProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.InventoryDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.ProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.ProductService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.web.rest.v1.ProductAdminController;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ProductAdminControllerTest {

  private MockMvc mockMvc;

  @Mock
  private ProductService productService;

  private ProductDTO productDTO;
  private CreateProductDTO createProductDTO;
  private InventoryDTO inventoryDTO;
  private ObjectMapper objectMapper;

  private ProductAdminController productAdminController;

  @BeforeEach
  void setUp() {
    // Initialize controller
    productAdminController = new ProductAdminController(productService);

    // Setup MockMvc
    mockMvc = MockMvcBuilders.standaloneSetup(productAdminController).build();

    // Initialize ObjectMapper for JSON conversion
    objectMapper = new ObjectMapper();

    // Setup test data
    productDTO = new ProductDTO();
    productDTO.setId(1L);
    productDTO.setName("Test Product");
    productDTO.setDescription("Test Description");
    productDTO.setPrice(BigDecimal.valueOf(99.99));
    productDTO.setAvailableQuantity(10);

    createProductDTO = new CreateProductDTO();
    createProductDTO.setName("Test Product");
    createProductDTO.setDescription("Test Description");
    createProductDTO.setPrice(BigDecimal.valueOf(99.99));
    createProductDTO.setQuantity(10);

    inventoryDTO = new InventoryDTO();
    inventoryDTO.setId(1L);
    inventoryDTO.setProductId(1L);
    inventoryDTO.setProductName("Test Product");
    inventoryDTO.setQuantity(15);
    inventoryDTO.setReservedQuantity(5);
    inventoryDTO.setAvailableQuantity(10);
  }

  @Test
  @DisplayName("Should create product successfully")
  void shouldCreateProductSuccessfully() throws Exception {
    // Given
    when(productService.save(any(CreateProductDTO.class))).thenReturn(productDTO);

    // When/Then
    mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProductDTO)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(productDTO.getId()))
           .andExpect(jsonPath("$.name").value(productDTO.getName()))
           .andExpect(jsonPath("$.description").value(productDTO.getDescription()))
           .andExpect(jsonPath("$.price").value(productDTO.getPrice().doubleValue()))
           .andExpect(jsonPath("$.availableQuantity").value(productDTO.getAvailableQuantity()));

    verify(productService).save(any(CreateProductDTO.class));
  }

  @Test
  @DisplayName("Should update product successfully")
  void shouldUpdateProductSuccessfully() throws Exception {
    // Given
    Long productId = 1L;
    createProductDTO.setId(productId);
    when(productService.update(any(CreateProductDTO.class))).thenReturn(productDTO);

    // When/Then
    mockMvc.perform(put("/api/v1/admin/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProductDTO)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(productDTO.getId()))
           .andExpect(jsonPath("$.name").value(productDTO.getName()))
           .andExpect(jsonPath("$.description").value(productDTO.getDescription()))
           .andExpect(jsonPath("$.price").value(productDTO.getPrice().doubleValue()))
           .andExpect(jsonPath("$.availableQuantity").value(productDTO.getAvailableQuantity()));

    verify(productService).update(any(CreateProductDTO.class));
  }

  @Test
  @DisplayName("Should delete product successfully")
  void shouldDeleteProductSuccessfully() throws Exception {
    // Given
    Long productId = 1L;
    doNothing().when(productService).delete(productId);

    // When/Then
    mockMvc.perform(delete("/api/v1/admin/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isNoContent());

    verify(productService).delete(productId);
  }

  @Test
  @DisplayName("Should update inventory successfully")
  void shouldUpdateInventorySuccessfully() throws Exception {
    // Given
    Long productId = 1L;
    inventoryDTO.setProductId(productId);
    when(productService.updateInventory(any(InventoryDTO.class))).thenReturn(inventoryDTO);

    // When/Then
    mockMvc.perform(put("/api/v1/admin/products/{id}/inventory", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryDTO)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(inventoryDTO.getId()))
           .andExpect(jsonPath("$.productId").value(inventoryDTO.getProductId()))
           .andExpect(jsonPath("$.productName").value(inventoryDTO.getProductName()))
           .andExpect(jsonPath("$.quantity").value(inventoryDTO.getQuantity()))
           .andExpect(jsonPath("$.reservedQuantity").value(inventoryDTO.getReservedQuantity()))
           .andExpect(jsonPath("$.availableQuantity").value(inventoryDTO.getAvailableQuantity()));

    verify(productService).updateInventory(any(InventoryDTO.class));
  }

  @Test
  @DisplayName("Should get low stock alerts successfully")
  void shouldGetLowStockAlertsSuccessfully() throws Exception {
    // Given
    int threshold = 5;
    List<InventoryDTO> lowStockItems = Arrays.asList(inventoryDTO);
    when(productService.getLowStockAlerts(threshold)).thenReturn(lowStockItems);

    // When/Then
    mockMvc.perform(get("/api/v1/admin/products/inventory/low-stock")
                        .param("threshold", String.valueOf(threshold))
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$").isArray())
           .andExpect(jsonPath("$.length()").value(1))
           .andExpect(jsonPath("$[0].id").value(inventoryDTO.getId()))
           .andExpect(jsonPath("$[0].productId").value(inventoryDTO.getProductId()))
           .andExpect(jsonPath("$[0].productName").value(inventoryDTO.getProductName()))
           .andExpect(jsonPath("$[0].quantity").value(inventoryDTO.getQuantity()))
           .andExpect(jsonPath("$[0].reservedQuantity").value(inventoryDTO.getReservedQuantity()))
           .andExpect(
               jsonPath("$[0].availableQuantity").value(inventoryDTO.getAvailableQuantity()));

    verify(productService).getLowStockAlerts(threshold);
  }
}