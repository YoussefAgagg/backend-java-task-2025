package com.gitthub.youssefagagg.ecommerceorderprocessor.user.web.rest.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.InventoryDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.ProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.ProductService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.web.rest.v1.ProductController;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  private MockMvc mockMvc;

  @Mock
  private ProductService productService;

  private ProductDTO product1;
  private ProductDTO product2;
  private InventoryDTO inventoryDTO;

  private ProductController productController;

  @BeforeEach
  void setUp() {
    // Initialize controller
    productController = new ProductController(productService);

    // Setup MockMvc with PageableHandlerMethodArgumentResolver to handle Pageable parameters
    mockMvc = MockMvcBuilders.standaloneSetup(productController)
                             .setCustomArgumentResolvers(
                                 new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                             .build();

    // Setup test data
    product1 = new ProductDTO();
    product1.setId(1L);
    product1.setName("Test Product 1");
    product1.setDescription("Test Description 1");
    product1.setPrice(BigDecimal.valueOf(99.99));
    product1.setAvailableQuantity(10);

    product2 = new ProductDTO();
    product2.setId(2L);
    product2.setName("Test Product 2");
    product2.setDescription("Test Description 2");
    product2.setPrice(BigDecimal.valueOf(149.99));
    product2.setAvailableQuantity(5);

    inventoryDTO = new InventoryDTO();
    inventoryDTO.setId(1L);
    inventoryDTO.setProductId(1L);
    inventoryDTO.setProductName("Test Product 1");
    inventoryDTO.setQuantity(15);
    inventoryDTO.setReservedQuantity(5);
    inventoryDTO.setAvailableQuantity(10);
  }

  @Test
  @DisplayName("Should get all products successfully")
  void shouldGetAllProductsSuccessfully() throws Exception {
    // Given
    List<ProductDTO> products = Arrays.asList(product1, product2);
    PaginationResponse<ProductDTO> paginationResponse = new PaginationResponse<>(
        products, 2L, 1, 0, 10
    );

    when(productService.findAll(any(Pageable.class))).thenReturn(paginationResponse);

    // When/Then
    mockMvc.perform(get("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data").isArray())
           .andExpect(jsonPath("$.data.length()").value(2))
           .andExpect(jsonPath("$.data[0].id").value(product1.getId()))
           .andExpect(jsonPath("$.data[0].name").value(product1.getName()))
           .andExpect(jsonPath("$.data[0].description").value(product1.getDescription()))
           .andExpect(jsonPath("$.data[0].price").value(product1.getPrice().doubleValue()))
           .andExpect(
               jsonPath("$.data[0].availableQuantity").value(product1.getAvailableQuantity()))
           .andExpect(jsonPath("$.data[1].id").value(product2.getId()))
           .andExpect(jsonPath("$.totalCount").value(2))
           .andExpect(jsonPath("$.noOfPages").value(1))
           .andExpect(jsonPath("$.pageNo").value(0))
           .andExpect(jsonPath("$.rowsPerPage").value(10));
  }

  @Test
  @DisplayName("Should get products by name successfully")
  void shouldGetProductsByNameSuccessfully() throws Exception {
    // Given
    List<ProductDTO> products = Arrays.asList(product1);
    PaginationResponse<ProductDTO> paginationResponse = new PaginationResponse<>(
        products, 1L, 1, 0, 10
    );

    when(productService.findByNameContaining(eq("Test Product 1"), any(Pageable.class)))
        .thenReturn(paginationResponse);

    // When/Then
    mockMvc.perform(get("/api/v1/products?name=Test Product 1")
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data").isArray())
           .andExpect(jsonPath("$.data.length()").value(1))
           .andExpect(jsonPath("$.data[0].id").value(product1.getId()))
           .andExpect(jsonPath("$.data[0].name").value(product1.getName()))
           .andExpect(jsonPath("$.totalCount").value(1));
  }

  @Test
  @DisplayName("Should get product by id successfully")
  void shouldGetProductByIdSuccessfully() throws Exception {
    // Given
    when(productService.findOne(1L)).thenReturn(product1);

    // When/Then
    mockMvc.perform(get("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(product1.getId()))
           .andExpect(jsonPath("$.name").value(product1.getName()))
           .andExpect(jsonPath("$.description").value(product1.getDescription()))
           .andExpect(jsonPath("$.price").value(product1.getPrice().doubleValue()))
           .andExpect(jsonPath("$.availableQuantity").value(product1.getAvailableQuantity()));
  }

  @Test
  @DisplayName("Should get inventory for a product successfully")
  void shouldGetInventoryForProductSuccessfully() throws Exception {
    // Given
    when(productService.getInventory(1L)).thenReturn(inventoryDTO);

    // When/Then
    mockMvc.perform(get("/api/v1/products/1/inventory")
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(inventoryDTO.getId()))
           .andExpect(jsonPath("$.productId").value(inventoryDTO.getProductId()))
           .andExpect(jsonPath("$.productName").value(inventoryDTO.getProductName()))
           .andExpect(jsonPath("$.quantity").value(inventoryDTO.getQuantity()))
           .andExpect(jsonPath("$.reservedQuantity").value(inventoryDTO.getReservedQuantity()))
           .andExpect(jsonPath("$.availableQuantity").value(inventoryDTO.getAvailableQuantity()));
  }
}
