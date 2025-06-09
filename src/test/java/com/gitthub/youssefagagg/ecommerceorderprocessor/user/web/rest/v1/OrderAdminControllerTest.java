package com.gitthub.youssefagagg.ecommerceorderprocessor.user.web.rest.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.DailySalesReportDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.OrderItemDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.OrderService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.web.rest.v1.OrderAdminController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
class OrderAdminControllerTest {

  private MockMvc mockMvc;

  @Mock
  private OrderService orderService;

  private OrderDTO orderDTO;
  private OrderItemDTO orderItemDTO;
  private DailySalesReportDTO dailySalesReportDTO;
  private ObjectMapper objectMapper;
  private PaginationResponse<OrderDTO> paginationResponse;

  private OrderAdminController orderAdminController;

  @BeforeEach
  void setUp() {
    // Initialize controller
    orderAdminController = new OrderAdminController(orderService);

    // Setup MockMvc with PageableHandlerMethodArgumentResolver to handle Pageable parameters
    mockMvc = MockMvcBuilders.standaloneSetup(orderAdminController)
                             .setCustomArgumentResolvers(
                                 new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
                             .build();

    // Initialize ObjectMapper for JSON conversion with JavaTimeModule for LocalDate serialization
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

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
    orderDTO.setStatus(OrderStatus.PAID);
    orderDTO.setTotalAmount(BigDecimal.valueOf(99.98));
    orderDTO.setOrderItems(orderItems);
    orderDTO.setIdempotencyKey("test-idempotency-key");

    List<OrderDTO> orders = new ArrayList<>();
    orders.add(orderDTO);

    paginationResponse = new PaginationResponse<>(
        orders,
        1L,
        1,
        0,
        10
    );

    dailySalesReportDTO = new DailySalesReportDTO();
    dailySalesReportDTO.setDate(LocalDate.now());
    dailySalesReportDTO.setTotalSales(BigDecimal.valueOf(99.98));
  }

  @Test
  @DisplayName("Should get all orders successfully")
  void shouldGetAllOrdersSuccessfully() throws Exception {
    // Given
    when(orderService.getAllOrders(any(Pageable.class))).thenReturn(paginationResponse);

    // When/Then
    mockMvc.perform(get("/api/v1/admin/orders")
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data").isArray())
           .andExpect(jsonPath("$.data.length()").value(1))
           .andExpect(jsonPath("$.data[0].id").value(orderDTO.getId()))
           .andExpect(jsonPath("$.data[0].userId").value(orderDTO.getUserId()))
           .andExpect(jsonPath("$.data[0].userName").value(orderDTO.getUserName()))
           .andExpect(jsonPath("$.data[0].status").value(orderDTO.getStatus().toString()))
           .andExpect(
               jsonPath("$.data[0].totalAmount").value(orderDTO.getTotalAmount().doubleValue()))
           .andExpect(jsonPath("$.totalCount").value(paginationResponse.totalCount()))
           .andExpect(jsonPath("$.noOfPages").value(paginationResponse.noOfPages()))
           .andExpect(jsonPath("$.pageNo").value(paginationResponse.pageNo()))
           .andExpect(jsonPath("$.rowsPerPage").value(paginationResponse.rowsPerPage()));

    verify(orderService).getAllOrders(any(Pageable.class));
  }

  @Test
  @DisplayName("Should get all orders by status successfully")
  void shouldGetAllOrdersByStatusSuccessfully() throws Exception {
    // Given
    OrderStatus status = OrderStatus.PAID;
    when(orderService.getAllOrdersByStatus(eq(status), any(Pageable.class))).thenReturn(
        paginationResponse);

    // When/Then
    mockMvc.perform(get("/api/v1/admin/orders")
                        .param("status", status.toString())
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data").isArray())
           .andExpect(jsonPath("$.data.length()").value(1))
           .andExpect(jsonPath("$.data[0].id").value(orderDTO.getId()))
           .andExpect(jsonPath("$.data[0].status").value(orderDTO.getStatus().toString()))
           .andExpect(jsonPath("$.totalCount").value(paginationResponse.totalCount()));

    verify(orderService).getAllOrdersByStatus(eq(status), any(Pageable.class));
  }

  @Test
  @DisplayName("Should update order status successfully")
  void shouldUpdateOrderStatusSuccessfully() throws Exception {
    // Given
    Long orderId = 1L;
    OrderStatus newStatus = OrderStatus.SHIPPED;

    // Create a copy of orderDTO with updated status
    OrderDTO updatedOrderDTO = new OrderDTO();
    updatedOrderDTO.setId(orderDTO.getId());
    updatedOrderDTO.setUserId(orderDTO.getUserId());
    updatedOrderDTO.setUserName(orderDTO.getUserName());
    updatedOrderDTO.setStatus(newStatus); // Updated status
    updatedOrderDTO.setTotalAmount(orderDTO.getTotalAmount());
    updatedOrderDTO.setOrderItems(orderDTO.getOrderItems());
    updatedOrderDTO.setIdempotencyKey(orderDTO.getIdempotencyKey());

    when(orderService.updateOrderStatus(eq(orderId), eq(newStatus))).thenReturn(updatedOrderDTO);

    // When/Then
    mockMvc.perform(put("/api/v1/admin/orders/{id}/status", orderId)
                        .param("status", newStatus.toString())
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(updatedOrderDTO.getId()))
           .andExpect(jsonPath("$.status").value(updatedOrderDTO.getStatus().toString()));

    verify(orderService).updateOrderStatus(eq(orderId), eq(newStatus));
  }

  @Test
  @DisplayName("Should get daily sales report successfully")
  void shouldGetDailySalesReportSuccessfully() throws Exception {
    // Given
    LocalDate startDate = LocalDate.now().minusDays(7);
    LocalDate endDate = LocalDate.now();
    List<DailySalesReportDTO> reportList = Arrays.asList(dailySalesReportDTO);

    when(orderService.getDailySalesReport(eq(startDate), eq(endDate))).thenReturn(reportList);

    // When/Then
    mockMvc.perform(get("/api/v1/admin/reports/daily")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$").isArray())
           .andExpect(jsonPath("$.length()").value(1))
           // Check date components individually since the date is serialized as an array [year, month, day]
           .andExpect(jsonPath("$[0].date[0]").value(dailySalesReportDTO.getDate().getYear()))
           .andExpect(jsonPath("$[0].date[1]").value(dailySalesReportDTO.getDate().getMonthValue()))
           .andExpect(jsonPath("$[0].date[2]").value(dailySalesReportDTO.getDate().getDayOfMonth()))
           .andExpect(jsonPath("$[0].totalSales").value(
               dailySalesReportDTO.getTotalSales().doubleValue()));

    verify(orderService).getDailySalesReport(eq(startDate), eq(endDate));
  }
}
