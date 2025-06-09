package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.gitthub.youssefagagg.ecommerceorderprocessor.TestcontainersConfiguration;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.OrderItemDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Inventory;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.InventoryRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.OrderItemRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.OrderRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.ProductRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.OrderService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class OrderServiceIT {

  @Autowired
  private OrderService orderService;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private InventoryRepository inventoryRepository;

  @Autowired
  private OrderItemRepository orderItemRepository;

  @Autowired
  private UserRepository userRepository;

  private User testUser;
  private User testAdminUser;
  private Product testProduct;
  private OrderDTO orderDTO;

  @BeforeEach
  void setUp() {
    // Clean up any existing data
    orderItemRepository.deleteAll();
    orderRepository.deleteAll();
    inventoryRepository.deleteAll();
    productRepository.deleteAll();

    // Create test user
    testUser = new User();
    testUser.setUsername("testuser");
    testUser.setEmail("testuser@example.com");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    // Password must be exactly 60 characters long
    testUser.setPassword("$2a$10$eDhncK/4cNH2KE.Y51AWpeL8/5TCTWBpxmVhvZuLfCPIb4SLQtEP6");
    testUser = userRepository.save(testUser);

    // Create test admin user
    testAdminUser = new User();
    testAdminUser.setUsername("testadmin");
    testAdminUser.setEmail("testadmin@example.com");
    testAdminUser.setFirstName("Admin");
    testAdminUser.setLastName("User");
    // Password must be exactly 60 characters long
    testAdminUser.setPassword("$2a$10$eDhncK/4cNH2KE.Y51AWpeL8/5TCTWBpxmVhvZuLfCPIb4SLQtEP6");
    testAdminUser = userRepository.save(testAdminUser);

    // Create test product with inventory
    testProduct = new Product();
    testProduct.setName("Test Product");
    testProduct.setDescription("Test Description");
    testProduct.setPrice(BigDecimal.valueOf(99.99));
    testProduct = productRepository.save(testProduct);

    // Create inventory for the product
    Inventory inventory = new Inventory();
    inventory.setProduct(testProduct);
    inventory.setQuantity(10);
    inventory.setReservedQuantity(0);
    inventory = inventoryRepository.save(inventory);

    // Link inventory to product
    testProduct.setInventory(inventory);
    testProduct = productRepository.save(testProduct);

    // Create order DTO for testing
    orderDTO = new OrderDTO();
    orderDTO.setUserId(testUser.getId());
    orderDTO.setIdempotencyKey(UUID.randomUUID().toString());

    // Create order item DTO
    OrderItemDTO orderItemDTO = new OrderItemDTO();
    orderItemDTO.setProductId(testProduct.getId());
    orderItemDTO.setQuantity(1);
    orderItemDTO.setPrice(testProduct.getPrice());
    orderItemDTO.setSubtotal(testProduct.getPrice());

    // Add order item to order
    List<OrderItemDTO> orderItems = new ArrayList<>();
    orderItems.add(orderItemDTO);
    orderDTO.setOrderItems(orderItems);
  }

  @Test
  @DisplayName("Should create order successfully")
  @WithMockUser(username = "testuser")
  void shouldCreateOrderSuccessfully() {
    // Verify initial inventory state
    Inventory initialInventory = inventoryRepository.findByProduct(testProduct).orElseThrow();
    assertThat(initialInventory.getReservedQuantity()).isEqualTo(0);
    assertThat(initialInventory.getQuantity()).isEqualTo(10);

    // When
    OrderDTO result = orderService.createOrder(orderDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    assertThat(result.getTotalAmount()).isEqualTo(testProduct.getPrice());

    // Verify order is persisted
    Order savedOrder = orderRepository.findById(result.getId()).orElseThrow();
    assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    assertThat(savedOrder.getTotalAmount()).isEqualTo(testProduct.getPrice());
    assertThat(savedOrder.getOrderItems()).hasSize(1);
    assertThat(savedOrder.getOrderItems().get(0).getProduct().getId()).isEqualTo(
        testProduct.getId());

    // Verify inventory is updated
    Inventory updatedInventory = inventoryRepository.findByProduct(testProduct).orElseThrow();
    assertThat(updatedInventory.getReservedQuantity()).isEqualTo(1);
    assertThat(updatedInventory.getQuantity()).isEqualTo(10); // Quantity should remain unchanged
    assertThat(updatedInventory.getAvailableQuantity()).isEqualTo(
        9); // Available quantity should be reduced
  }

  @Test
  @DisplayName("Should get current user orders")
  @WithMockUser(username = "testuser")
  void shouldGetCurrentUserOrders() {
    // Given
    OrderDTO createdOrder = orderService.createOrder(orderDTO);
    Pageable pageable = PageRequest.of(0, 10);

    // When
    PaginationResponse<OrderDTO> result = orderService.getCurrentUserOrders(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).isNotEmpty();
    assertThat(result.data().get(0).getId()).isEqualTo(createdOrder.getId());
    assertThat(result.data().get(0).getUserId()).isEqualTo(testUser.getId());
  }

  @Test
  @DisplayName("Should get order by ID")
  @WithMockUser(username = "testuser")
  void shouldGetOrderById() {
    // Given
    OrderDTO createdOrder = orderService.createOrder(orderDTO);

    // When
    OrderDTO result = orderService.getOrder(createdOrder.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(createdOrder.getId());
    assertThat(result.getUserId()).isEqualTo(testUser.getId());
    assertThat(result.getStatus()).isEqualTo(createdOrder.getStatus());
  }

  @Test
  @DisplayName("Should cancel order")
  @WithMockUser(username = "testuser")
  void shouldCancelOrder() {
    // Given
    OrderDTO createdOrder = orderService.createOrder(orderDTO);
    Order order = orderRepository.findById(createdOrder.getId()).orElseThrow();
    order.setStatus(OrderStatus.PROCESSING);
    orderRepository.save(order);

    // Verify initial inventory state
    Inventory initialInventory = inventoryRepository.findByProduct(testProduct).orElseThrow();
    assertThat(initialInventory.getReservedQuantity()).isEqualTo(1);
    assertThat(initialInventory.getQuantity()).isEqualTo(10);

    // When
    OrderDTO result = orderService.cancelOrder(createdOrder.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);

    // Verify order is updated in database
    Order savedOrder = orderRepository.findById(result.getId()).orElseThrow();
    assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);

    // Verify inventory is released
    Inventory updatedInventory = inventoryRepository.findByProduct(testProduct).orElseThrow();
    assertThat(updatedInventory.getReservedQuantity()).isEqualTo(0);
    assertThat(updatedInventory.getQuantity()).isEqualTo(10); // Quantity should remain unchanged
  }

  @Test
  @DisplayName("Should get all orders (admin)")
  @WithMockUser(username = "testadmin",
                roles = {"ADMIN"})
  void shouldGetAllOrders() {
    // Given
    OrderDTO createdOrder = orderService.createOrder(orderDTO);
    Pageable pageable = PageRequest.of(0, 10);

    // When
    PaginationResponse<OrderDTO> result = orderService.getAllOrders(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).isNotEmpty();
    assertThat(result.data().get(0).getId()).isEqualTo(createdOrder.getId());
  }

  @Test
  @DisplayName("Should get all orders by status (admin)")
  @WithMockUser(username = "testadmin",
                roles = {"ADMIN"})
  void shouldGetAllOrdersByStatus() {
    // Given
    OrderDTO createdOrder = orderService.createOrder(orderDTO);
    Pageable pageable = PageRequest.of(0, 10);

    // When
    PaginationResponse<OrderDTO> result = orderService.getAllOrdersByStatus(OrderStatus.PROCESSING,
                                                                            pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).isNotEmpty();
    assertThat(result.data().get(0).getId()).isEqualTo(createdOrder.getId());
    assertThat(result.data().get(0).getStatus()).isEqualTo(OrderStatus.PROCESSING);
  }

  @Test
  @DisplayName("Should get current user orders by status")
  @WithMockUser(username = "testuser")
  void shouldGetCurrentUserOrdersByStatus() {
    // Given
    OrderDTO createdOrder = orderService.createOrder(orderDTO);
    Pageable pageable = PageRequest.of(0, 10);

    // When
    PaginationResponse<OrderDTO> result = orderService.getCurrentUserOrdersByStatus(
        OrderStatus.PROCESSING, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).isNotEmpty();
    assertThat(result.data().get(0).getId()).isEqualTo(createdOrder.getId());
    assertThat(result.data().get(0).getStatus()).isEqualTo(OrderStatus.PROCESSING);
  }

  @Test
  @DisplayName("Should update order status (admin)")
  @WithMockUser(username = "testadmin",
                roles = {"ADMIN"})
  void shouldUpdateOrderStatus() {
    // Given
    OrderDTO createdOrder = orderService.createOrder(orderDTO);
    OrderStatus newStatus = OrderStatus.SHIPPED;

    // When
    OrderDTO result = orderService.updateOrderStatus(createdOrder.getId(), newStatus);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(newStatus);

    // Verify order is updated in database
    Order savedOrder = orderRepository.findById(result.getId()).orElseThrow();
    assertThat(savedOrder.getStatus()).isEqualTo(newStatus);

    // Verify inventory is unchanged for SHIPPED status
    Inventory updatedInventory = inventoryRepository.findByProduct(testProduct).orElseThrow();
    assertThat(updatedInventory.getReservedQuantity()).isEqualTo(1);
    assertThat(updatedInventory.getQuantity()).isEqualTo(10);
  }

  @Test
  @DisplayName("Should get order status")
  @WithMockUser(username = "testuser")
  void shouldGetOrderStatus() {
    // Given
    OrderDTO createdOrder = orderService.createOrder(orderDTO);

    // When
    OrderStatus result = orderService.getOrderStatus(createdOrder.getId());

    // Then
    assertThat(result).isEqualTo(OrderStatus.PROCESSING);
  }

  @Test
  @DisplayName("Should update order status to DELIVERED and fulfill inventory")
  @WithMockUser(username = "testadmin",
                roles = {"ADMIN"})
  void shouldUpdateOrderStatusToDeliveredAndFulfillInventory() {
    // Given
    OrderDTO createdOrder = orderService.createOrder(orderDTO);
    OrderStatus newStatus = OrderStatus.DELIVERED;

    // When
    OrderDTO result = orderService.updateOrderStatus(createdOrder.getId(), newStatus);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(newStatus);

    // Verify order is updated in database
    Order savedOrder = orderRepository.findById(result.getId()).orElseThrow();
    assertThat(savedOrder.getStatus()).isEqualTo(newStatus);

    // Verify inventory is fulfilled for DELIVERED status (both quantity and reservedQuantity reduced)
    Inventory updatedInventory = inventoryRepository.findByProduct(testProduct).orElseThrow();
    assertThat(updatedInventory.getReservedQuantity()).isEqualTo(0);
    assertThat(updatedInventory.getQuantity()).isEqualTo(9); // 10 - 1
  }

  // These tests are now covered by more focused tests:
  // shouldVerifyInventoryAfterCancellation
  // shouldVerifyInventoryAfterPaidStatus

  @Test
  @DisplayName("Should verify inventory after DELIVERED status")
  @WithMockUser(username = "testadmin",
                roles = {"ADMIN"})
  void shouldVerifyInventoryAfterDeliveredStatus() {
    // Verify initial inventory state
    Inventory initialInventory = inventoryRepository.findByProduct(testProduct).orElseThrow();
    assertThat(initialInventory.getReservedQuantity()).isEqualTo(0);
    assertThat(initialInventory.getQuantity()).isEqualTo(10);

    // Create order
    OrderDTO createdOrder = orderService.createOrder(orderDTO);

    // Update to DELIVERED status
    OrderDTO deliveredOrder = orderService.updateOrderStatus(createdOrder.getId(),
                                                             OrderStatus.DELIVERED);
    assertThat(deliveredOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);

    // Verify inventory after DELIVERED status (both quantity and reservedQuantity reduced)
    Inventory inventoryAfterDelivered = inventoryRepository.findByProduct(testProduct)
                                                           .orElseThrow();
    assertThat(inventoryAfterDelivered.getReservedQuantity()).isEqualTo(0);
    assertThat(inventoryAfterDelivered.getQuantity()).isEqualTo(9);
    assertThat(inventoryAfterDelivered.getAvailableQuantity()).isEqualTo(9);
  }

  @Test
  @DisplayName("Should verify inventory after SHIPPED status")
  @WithMockUser(username = "testadmin",
                roles = {"ADMIN"})
  void shouldVerifyInventoryAfterShippedStatus() {
    // Verify initial inventory state
    Inventory initialInventory = inventoryRepository.findByProduct(testProduct).orElseThrow();
    assertThat(initialInventory.getReservedQuantity()).isEqualTo(0);
    assertThat(initialInventory.getQuantity()).isEqualTo(10);

    // Create order
    OrderDTO createdOrder = orderService.createOrder(orderDTO);

    // Update to SHIPPED status
    OrderDTO shippedOrder = orderService.updateOrderStatus(createdOrder.getId(),
                                                           OrderStatus.SHIPPED);
    assertThat(shippedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);

    // Verify inventory after SHIPPED status (reservedQuantity should remain, quantity unchanged)
    Inventory inventoryAfterShipped = inventoryRepository.findByProduct(testProduct).orElseThrow();
    assertThat(inventoryAfterShipped.getReservedQuantity()).isEqualTo(1);
    assertThat(inventoryAfterShipped.getQuantity()).isEqualTo(10);
    assertThat(inventoryAfterShipped.getAvailableQuantity()).isEqualTo(9);
  }

}
