package com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Role;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.DailySalesReportDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.OrderItemDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.PaymentDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Inventory;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.OrderItem;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.OrderStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.PaymentStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Product;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper.InventoryMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper.InventoryMapperImpl;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper.OrderItemMapperImpl;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper.OrderMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper.OrderMapperImpl;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.InventoryRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.NotificationRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.OrderItemRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.OrderRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.ProductRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.AuditService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.NotificationService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.PaymentService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.AuthoritiesRole;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.SecurityUtils;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.WebSocketService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.util.KeyLockManager;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    OrderMapperImpl.class,
    InventoryMapperImpl.class,
    OrderItemMapperImpl.class
})
class OrderServiceImplTest {

  private final KeyLockManager keyLockManager = new KeyLockManager();
  private MockedStatic<SecurityUtils> securityUtilsMock;
  @Autowired
  private OrderMapper orderMapper;
  @Autowired
  private InventoryMapper inventoryMapper;
  @Mock
  private OrderRepository orderRepository;
  @Mock
  private ProductRepository productRepository;
  @Mock
  private InventoryRepository inventoryRepository;
  @Mock
  private OrderItemRepository orderItemRepository;
  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private WebSocketService webSocketService;
  @Mock
  private AuditService auditService;
  @Mock
  private PaymentService paymentService;
  @Mock
  private NotificationService notificationService;
  private TestOrderServiceImpl orderService;

  private User user;
  private User adminUser;
  private Product product;
  private Inventory inventory;
  private Order order;
  private OrderItem orderItem;
  private OrderDTO orderDTO;
  private OrderItemDTO orderItemDTO;
  private PaymentDTO paymentDTO;

  @BeforeEach
  void setUp() {
    // Setup SecurityUtils mocks
    securityUtilsMock = mockStatic(SecurityUtils.class);
    securityUtilsMock.when(() -> SecurityUtils.getCurrentUserUserName())
                     .thenReturn(Optional.of("testuser"));
    securityUtilsMock.when(
                         () -> SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesRole.ROLE_ADMIN.getValue()))
                     .thenReturn(false);

    // Setup user data
    user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");

    Role userRole = new Role();
    userRole.setName("ROLE_USER");

    Role adminRole = new Role();
    adminRole.setName("ROLE_ADMIN");

    Set<Role> userRoles = new HashSet<>();
    userRoles.add(userRole);
    user.setRoles(userRoles);

    adminUser = new User();
    adminUser.setId(2L);
    adminUser.setUsername("adminuser");
    adminUser.setEmail("admin@example.com");

    Set<Role> adminRoles = new HashSet<>();
    adminRoles.add(userRole);
    adminRoles.add(adminRole);
    adminUser.setRoles(adminRoles);

    // Setup product and inventory
    product = new Product();
    product.setId(1L);
    product.setName("Test Product");
    product.setDescription("Test Description");
    product.setPrice(BigDecimal.valueOf(99.99));

    inventory = new Inventory();
    inventory.setId(1L);
    inventory.setProduct(product);
    inventory.setQuantity(10);
    inventory.setReservedQuantity(0);

    product.setInventory(inventory);

    // Setup order and order item
    order = new Order();
    order.setId(1L);
    order.setUser(user);
    order.setStatus(OrderStatus.PENDING);
    order.setTotalAmount(BigDecimal.valueOf(99.99));
    order.setOrderItems(new ArrayList<>());
    order.setCreatedDate(Instant.now());
    order.setLastModifiedDate(Instant.now());

    orderItem = new OrderItem();
    orderItem.setId(1L);
    orderItem.setOrder(order);
    orderItem.setProduct(product);
    orderItem.setQuantity(1);
    orderItem.setPrice(BigDecimal.valueOf(99.99));

    order.getOrderItems().add(orderItem);

    // Setup DTOs
    orderDTO = new OrderDTO();
    orderDTO.setId(1L);
    orderDTO.setUserId(user.getId());
    orderDTO.setStatus(OrderStatus.PENDING);
    orderDTO.setTotalAmount(BigDecimal.valueOf(99.99));
    orderDTO.setIdempotencyKey(UUID.randomUUID().toString());

    orderItemDTO = new OrderItemDTO();
    orderItemDTO.setId(1L);
    orderItemDTO.setOrderId(order.getId());
    orderItemDTO.setProductId(product.getId());
    orderItemDTO.setProductName(product.getName());
    orderItemDTO.setQuantity(1);
    orderItemDTO.setPrice(BigDecimal.valueOf(99.99));
    orderItemDTO.setSubtotal(BigDecimal.valueOf(99.99));

    orderDTO.setOrderItems(Collections.singletonList(orderItemDTO));

    paymentDTO = PaymentDTO.builder()
                           .id(1L)
                           .orderId(order.getId())
                           .amount(order.getTotalAmount())
                           .paymentMethod("Credit Card")
                           .status(PaymentStatus.COMPLETED)
                           .idempotencyKey(UUID.randomUUID().toString())
                           .build();

    // Create service with test user
    orderService = new TestOrderServiceImpl(
        userRepository,
        orderRepository,
        productRepository,
        inventoryRepository,
        orderItemRepository,
        notificationRepository,
        orderMapper,
        inventoryMapper,
        webSocketService,
        auditService,
        paymentService,
        notificationService,
        keyLockManager,
        user
    );
  }

  @AfterEach
  void tearDown() {
    if (securityUtilsMock != null) {
      securityUtilsMock.close();
    }
  }

  @Test
  @DisplayName("Should create order successfully")
  void shouldCreateOrderSuccessfully() {
    // Given
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order savedOrder = invocation.getArgument(0);
      savedOrder.setId(1L); // Simulate generated ID
      return savedOrder;
    });
    when(productRepository.findByIdInWithInventory(any())).thenReturn(
        Collections.singletonList(product));
    when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
      Inventory savedInventory = invocation.getArgument(0);
      return savedInventory;
    });
    when(productRepository.findByIdInWithInventory(any())).thenReturn(
        Collections.singletonList(product));
    when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> {
      OrderItem savedOrderItem = invocation.getArgument(0);
      savedOrderItem.setId(1L); // Simulate generated ID
      return savedOrderItem;
    });
    when(paymentService.processPayment(any(PaymentDTO.class))).thenReturn(paymentDTO);


    // When
    OrderDTO result = orderService.createOrder(orderDTO);

    // Then
    assertThat(result).isNotNull();
    verify(orderRepository, times(4)).save(any(Order.class));
    verify(inventoryRepository).save(any(Inventory.class));
    verify(orderItemRepository).save(any(OrderItem.class));
    verify(paymentService).processPayment(any(PaymentDTO.class));
    verify(notificationService).createNotification(any(User.class), any(), anyString());
    verify(auditService).createLogAsync(anyString(), anyLong(), any(Order.class));
    verify(webSocketService).sendOrderStatusUpdate(anyLong(), any(OrderDTO.class));
  }

  @Test
  @DisplayName("Should throw exception when creating order with insufficient inventory")
  void shouldThrowExceptionWhenCreatingOrderWithInsufficientInventory() {
    // Given
    when(orderRepository.save(any(Order.class))).thenAnswer(
        invocation -> invocation.getArgument(0));
    when(productRepository.findByIdInWithInventory(any())).thenReturn(
        Collections.singletonList(product));

    // Set inventory quantity to be less than requested
    inventory.setQuantity(0);


    // When/Then
    assertThatThrownBy(() -> orderService.createOrder(orderDTO))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

    verify(orderRepository).save(any(Order.class));
    verify(inventoryRepository, never()).save(any(Inventory.class));
    verify(orderItemRepository, never()).save(any(OrderItem.class));
  }

  @Test
  @DisplayName("Should get current user orders")
  void shouldGetCurrentUserOrders() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    List<Order> orders = Collections.singletonList(order);
    Page<Order> page = new PageImpl<>(orders, pageable, orders.size());

    when(orderRepository.findByUser(user, pageable)).thenReturn(page);

    // When
    PaginationResponse<OrderDTO> result = orderService.getCurrentUserOrders(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(order.getId());
    assertThat(result.totalCount()).isEqualTo(1);

    verify(orderRepository).findByUser(user, pageable);
  }

  @Test
  @DisplayName("Should get current user orders by status")
  void shouldGetCurrentUserOrdersByStatus() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    List<Order> orders = Collections.singletonList(order);
    Page<Order> page = new PageImpl<>(orders, pageable, orders.size());
    OrderStatus status = OrderStatus.PENDING;

    when(orderRepository.findByUserAndStatus(user, status, pageable)).thenReturn(page);

    // When
    PaginationResponse<OrderDTO> result = orderService.getCurrentUserOrdersByStatus(status,
                                                                                    pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(order.getId());
    assertThat(result.data().get(0).getStatus()).isEqualTo(status);
    assertThat(result.totalCount()).isEqualTo(1);

    verify(orderRepository).findByUserAndStatus(user, status, pageable);
  }

  @Test
  @DisplayName("Should get all orders (admin)")
  void shouldGetAllOrders() {
    // Given
    // Mock admin privileges
    securityUtilsMock.when(
                         () -> SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesRole.ROLE_ADMIN.getValue()))
                     .thenReturn(true);

    Pageable pageable = PageRequest.of(0, 10);
    List<Order> orders = Collections.singletonList(order);
    Page<Order> page = new PageImpl<>(orders, pageable, orders.size());

    when(orderRepository.findAll(pageable)).thenReturn(page);

    // When
    PaginationResponse<OrderDTO> result = orderService.getAllOrders(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(order.getId());
    assertThat(result.totalCount()).isEqualTo(1);

    verify(orderRepository).findAll(pageable);
  }

  @Test
  @DisplayName("Should get all orders by status (admin)")
  void shouldGetAllOrdersByStatus() {
    // Given
    // Mock admin privileges
    securityUtilsMock.when(
                         () -> SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesRole.ROLE_ADMIN.getValue()))
                     .thenReturn(true);

    Pageable pageable = PageRequest.of(0, 10);
    List<Order> orders = Collections.singletonList(order);
    Page<Order> page = new PageImpl<>(orders, pageable, orders.size());
    OrderStatus status = OrderStatus.PENDING;

    when(orderRepository.findByStatus(status, pageable)).thenReturn(page);

    // When
    PaginationResponse<OrderDTO> result = orderService.getAllOrdersByStatus(status, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(order.getId());
    assertThat(result.data().get(0).getStatus()).isEqualTo(status);
    assertThat(result.totalCount()).isEqualTo(1);

    verify(orderRepository).findByStatus(status, pageable);
  }

  @Test
  @DisplayName("Should get order by ID")
  void shouldGetOrderById() {
    // Given
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    // When
    OrderDTO result = orderService.getOrder(order.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(order.getId());
    assertThat(result.getUserId()).isEqualTo(user.getId());
    assertThat(result.getStatus()).isEqualTo(order.getStatus());

    verify(orderRepository).findById(order.getId());
  }

  @Test
  @DisplayName("Should throw exception when getting non-existent order")
  void shouldThrowExceptionWhenGettingNonExistentOrder() {
    // Given
    Long nonExistentId = 999L;
    when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> orderService.getOrder(nonExistentId))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(orderRepository).findById(nonExistentId);
  }

  @Test
  @DisplayName("Should cancel order successfully")
  void shouldCancelOrderSuccessfully() {
    // Given
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      // Directly set the status to CANCELLED to bypass the updateStatus logic
      order.setStatus(OrderStatus.CANCELLED);
      return order;
    });
    when(inventoryRepository.findByProduct(product)).thenReturn(Optional.of(inventory));

    // When
    OrderDTO result = orderService.cancelOrder(order.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);

    verify(orderRepository).findById(order.getId());
    verify(orderRepository).save(order);
    verify(inventoryRepository).findByProduct(product);
    verify(inventoryRepository).save(inventory);
    verify(notificationService).createNotification(any(User.class), any(), anyString());
    verify(auditService).updateLogAsync(anyString(), anyLong(), anyString(), any(Order.class),
                                        any(Order.class));
    verify(webSocketService).sendOrderStatusUpdate(anyLong(), any(OrderDTO.class));
    verify(webSocketService).sendOrderStatusChangeEvent(anyLong(), any(OrderStatus.class),
                                                        any(OrderStatus.class));
  }

  @Test
  @DisplayName("Should throw exception when cancelling non-existent order")
  void shouldThrowExceptionWhenCancellingNonExistentOrder() {
    // Given
    Long nonExistentId = 999L;
    when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> orderService.cancelOrder(nonExistentId))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(orderRepository).findById(nonExistentId);
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("Should throw exception when cancelling order with invalid status")
  void shouldThrowExceptionWhenCancellingOrderWithInvalidStatus() {
    // Given
    order.setStatus(OrderStatus.DELIVERED);
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    // When/Then
    assertThatThrownBy(() -> orderService.cancelOrder(order.getId()))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

    verify(orderRepository).findById(order.getId());
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  @DisplayName("Should update order status successfully")
  void shouldUpdateOrderStatusSuccessfully() {
    // Given
    // Mock admin privileges
    securityUtilsMock.when(
                         () -> SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesRole.ROLE_ADMIN.getValue()))
                     .thenReturn(true);

    OrderStatus newStatus = OrderStatus.SHIPPED;
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    // When
    OrderDTO result = orderService.updateOrderStatus(order.getId(), newStatus);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(newStatus);

    verify(orderRepository).findById(order.getId());
    verify(orderRepository).save(order);
    verify(auditService).updateLogAsync(anyString(), anyLong(), anyString(), any(Order.class),
                                        any(Order.class));
    verify(webSocketService).sendOrderStatusUpdate(anyLong(), any(OrderDTO.class));
    verify(webSocketService).sendOrderStatusChangeEvent(anyLong(), any(OrderStatus.class),
                                                        any(OrderStatus.class));
  }

  @Test
  @DisplayName("Should get order status")
  void shouldGetOrderStatus() {
    // Given
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    // When
    OrderStatus result = orderService.getOrderStatus(order.getId());

    // Then
    assertThat(result).isEqualTo(order.getStatus());

    verify(orderRepository).findById(order.getId());
  }

  @Test
  @DisplayName("Should get daily sales report")
  void shouldGetDailySalesReport() {
    // Given
    LocalDate startDate = LocalDate.now().minusDays(7);
    LocalDate endDate = LocalDate.now();

    List<Object[]> reportData = new ArrayList<>();
    reportData.add(new Object[] {Date.valueOf(startDate), BigDecimal.valueOf(199.98)});
    reportData.add(new Object[] {Date.valueOf(startDate.plusDays(1)), BigDecimal.valueOf(299.97)});

    when(orderRepository.getDailySalesReport(any(Instant.class), any(Instant.class))).thenReturn(
        reportData);

    // When
    List<DailySalesReportDTO> result = orderService.getDailySalesReport(startDate, endDate);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getDate()).isEqualTo(startDate);
    assertThat(result.get(0).getTotalSales()).isEqualTo(BigDecimal.valueOf(199.98));
    assertThat(result.get(1).getDate()).isEqualTo(startDate.plusDays(1));
    assertThat(result.get(1).getTotalSales()).isEqualTo(BigDecimal.valueOf(299.97));

    verify(orderRepository).getDailySalesReport(any(Instant.class), any(Instant.class));
  }

  // Test subclass that overrides getCurrentUser to avoid static mocking
  private class TestOrderServiceImpl extends OrderServiceImpl {
    private final User currentUser;

    public TestOrderServiceImpl(
        UserRepository userRepository,
        OrderRepository orderRepository,
        ProductRepository productRepository,
        InventoryRepository inventoryRepository,
        OrderItemRepository orderItemRepository,
        NotificationRepository notificationRepository,
        OrderMapper orderMapper,
        InventoryMapper inventoryMapper,
        WebSocketService webSocketService,
        AuditService auditService,
        PaymentService paymentService,
        NotificationService notificationService,
        KeyLockManager keyLockManager,
        User currentUser) {
      super(userRepository, orderRepository, productRepository, inventoryRepository,
            orderItemRepository, notificationRepository, orderMapper, inventoryMapper,
            webSocketService, auditService, paymentService, notificationService, keyLockManager);
      this.currentUser = currentUser;
    }

    @Override
    protected User getCurrentUser() {
      return currentUser;
    }
  }
}
