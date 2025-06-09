package com.gitthub.youssefagagg.ecommerceorderprocessor.product.web.rest.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.OrderItemDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.OrderStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.OrderService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

  private MockMvc mockMvc;

  @Mock
  private OrderService orderService;

  private OrderDTO orderDTO;
  private OrderItemDTO orderItemDTO;
  private ObjectMapper objectMapper;

  private OrderController orderController;

  @BeforeEach
  void setUp() {
    // Initialize controller
    orderController = new OrderController(orderService);

    // Setup MockMvc with PageableHandlerMethodArgumentResolver to handle Pageable parameters
    mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                             .setCustomArgumentResolvers(
                                 new PageableHandlerMethodArgumentResolver())
                             .build();

    // Initialize ObjectMapper for JSON conversion
    objectMapper = new ObjectMapper();

    // Setup test data
    orderItemDTO = new OrderItemDTO();
    orderItemDTO.setId(1L);
    orderItemDTO.setOrderId(1L);
    orderItemDTO.setProductId(1L);
    orderItemDTO.setProductName("Test Product");
    orderItemDTO.setQuantity(2);
    orderItemDTO.setPrice(BigDecimal.valueOf(49.99));
    orderItemDTO.setSubtotal(BigDecimal.valueOf(99.98));

    List<OrderItemDTO> orderItems = new ArrayList<>();
    orderItems.add(orderItemDTO);

    orderDTO = new OrderDTO();
    orderDTO.setId(1L);
    orderDTO.setUserId(1L);
    orderDTO.setUserName("Test User");
    orderDTO.setStatus(OrderStatus.PENDING);
    orderDTO.setTotalAmount(BigDecimal.valueOf(99.98));
    orderDTO.setOrderItems(orderItems);
    orderDTO.setIdempotencyKey(UUID.randomUUID().toString());
  }

  @Test
  @DisplayName("Should create order successfully")
  void shouldCreateOrderSuccessfully() throws Exception {
    // Given
    when(orderService.createOrder(any(OrderDTO.class))).thenReturn(orderDTO);

    // When/Then
    mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(orderDTO.getId()))
           .andExpect(jsonPath("$.userId").value(orderDTO.getUserId()))
           .andExpect(jsonPath("$.userName").value(orderDTO.getUserName()))
           .andExpect(jsonPath("$.status").value(orderDTO.getStatus().toString()))
           .andExpect(jsonPath("$.totalAmount").value(orderDTO.getTotalAmount().doubleValue()))
           .andExpect(jsonPath("$.orderItems").isArray())
           .andExpect(jsonPath("$.orderItems.length()").value(1))
           .andExpect(jsonPath("$.orderItems[0].id").value(orderItemDTO.getId()))
           .andExpect(jsonPath("$.orderItems[0].productId").value(orderItemDTO.getProductId()))
           .andExpect(jsonPath("$.orderItems[0].productName").value(orderItemDTO.getProductName()))
           .andExpect(jsonPath("$.orderItems[0].quantity").value(orderItemDTO.getQuantity()))
           .andExpect(
               jsonPath("$.orderItems[0].price").value(orderItemDTO.getPrice().doubleValue()))
           .andExpect(jsonPath("$.orderItems[0].subtotal").value(
               orderItemDTO.getSubtotal().doubleValue()));
  }

  @Test
  @DisplayName("Should get all orders for current user successfully")
  void shouldGetAllOrdersForCurrentUserSuccessfully() throws Exception {
    // Given
    List<OrderDTO> orders = List.of(orderDTO);
    PaginationResponse<OrderDTO> paginationResponse = new PaginationResponse<>(
        orders, 1L, 1, 0, 10
    );

    when(orderService.getCurrentUserOrders(any(Pageable.class))).thenReturn(paginationResponse);

    // When/Then
    mockMvc.perform(get("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data").isArray())
           .andExpect(jsonPath("$.data.length()").value(1))
           .andExpect(jsonPath("$.data[0].id").value(orderDTO.getId()))
           .andExpect(jsonPath("$.data[0].userId").value(orderDTO.getUserId()))
           .andExpect(jsonPath("$.data[0].userName").value(orderDTO.getUserName()))
           .andExpect(jsonPath("$.data[0].status").value(orderDTO.getStatus().toString()))
           .andExpect(jsonPath("$.totalCount").value(1))
           .andExpect(jsonPath("$.noOfPages").value(1))
           .andExpect(jsonPath("$.pageNo").value(0))
           .andExpect(jsonPath("$.rowsPerPage").value(10));
  }

  @Test
  @DisplayName("Should get all orders for current user by status successfully")
  void shouldGetAllOrdersForCurrentUserByStatusSuccessfully() throws Exception {
    // Given
    List<OrderDTO> orders = List.of(orderDTO);
    PaginationResponse<OrderDTO> paginationResponse = new PaginationResponse<>(
        orders, 1L, 1, 0, 10
    );

    when(orderService.getCurrentUserOrdersByStatus(eq(OrderStatus.PENDING), any(Pageable.class)))
        .thenReturn(paginationResponse);

    // When/Then
    mockMvc.perform(get("/api/v1/orders")
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data").isArray())
           .andExpect(jsonPath("$.data.length()").value(1))
           .andExpect(jsonPath("$.data[0].id").value(orderDTO.getId()))
           .andExpect(jsonPath("$.data[0].status").value(orderDTO.getStatus().toString()))
           .andExpect(jsonPath("$.totalCount").value(1));
  }

  @Test
  @DisplayName("Should get order by id successfully")
  void shouldGetOrderByIdSuccessfully() throws Exception {
    // Given
    when(orderService.getOrder(1L)).thenReturn(orderDTO);

    // When/Then
    mockMvc.perform(get("/api/v1/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(orderDTO.getId()))
           .andExpect(jsonPath("$.userId").value(orderDTO.getUserId()))
           .andExpect(jsonPath("$.userName").value(orderDTO.getUserName()))
           .andExpect(jsonPath("$.status").value(orderDTO.getStatus().toString()))
           .andExpect(jsonPath("$.totalAmount").value(orderDTO.getTotalAmount().doubleValue()))
           .andExpect(jsonPath("$.orderItems").isArray())
           .andExpect(jsonPath("$.orderItems.length()").value(1));
  }

  @Test
  @DisplayName("Should cancel order successfully")
  void shouldCancelOrderSuccessfully() throws Exception {
    // Given
    OrderDTO cancelledOrder = new OrderDTO();
    cancelledOrder.setId(1L);
    cancelledOrder.setUserId(1L);
    cancelledOrder.setUserName("Test User");
    cancelledOrder.setStatus(OrderStatus.CANCELLED);
    cancelledOrder.setTotalAmount(BigDecimal.valueOf(99.98));
    cancelledOrder.setOrderItems(orderDTO.getOrderItems());
    cancelledOrder.setIdempotencyKey(orderDTO.getIdempotencyKey());

    when(orderService.cancelOrder(1L)).thenReturn(cancelledOrder);

    // When/Then
    mockMvc.perform(put("/api/v1/orders/{id}/cancel", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(cancelledOrder.getId()))
           .andExpect(jsonPath("$.status").value(cancelledOrder.getStatus().toString()));
  }

  @Test
  @DisplayName("Should get order status successfully")
  void shouldGetOrderStatusSuccessfully() throws Exception {
    // Given
    when(orderService.getOrderStatus(1L)).thenReturn(OrderStatus.PENDING);

    // When/Then
    mockMvc.perform(get("/api/v1/orders/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$").value(OrderStatus.PENDING.toString()));
  }
}
