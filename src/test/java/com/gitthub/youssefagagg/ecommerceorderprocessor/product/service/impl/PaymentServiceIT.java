package com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gitthub.youssefagagg.ecommerceorderprocessor.TestcontainersConfiguration;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.OrderItemDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.PaymentDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Inventory;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.OrderItem;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.OrderStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Payment;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.PaymentStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Product;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.InventoryRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.OrderItemRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.OrderRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.PaymentRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.ProductRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.PaymentService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class PaymentServiceIT {

  @Autowired
  private PaymentService paymentService;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderItemRepository orderItemRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private InventoryRepository inventoryRepository;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private UserRepository userRepository;

  private User testUser;
  private User testAdminUser;
  private Product testProduct;
  private OrderDTO orderDTO;
  private Order order;
  private PaymentDTO paymentDTO;

  @BeforeEach
  void setUp() {
    // Clean up any existing data
    paymentRepository.deleteAll();
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

    // Create order directly using repository
    Order order = new Order();
    order.setUser(testUser);
    order.setStatus(OrderStatus.PROCESSING);
    order.setTotalAmount(testProduct.getPrice());
    order.setIdempotencyKey(orderDTO.getIdempotencyKey());
    order = orderRepository.save(order);

    // Create order item
    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(testProduct);
    orderItem.setQuantity(orderItemDTO.getQuantity());
    orderItem.setPrice(orderItemDTO.getPrice());
    orderItemRepository.save(orderItem);

    // Update orderDTO with saved order ID
    orderDTO.setId(order.getId());
  }

  @Test
  @DisplayName("Should process payment successfully")
  @WithMockUser(username = "testuser")
  void shouldProcessPaymentSuccessfully() {
    // Create an order directly using repository
    Order order = new Order();
    order.setUser(testUser);
    order.setStatus(OrderStatus.PROCESSING);
    order.setTotalAmount(testProduct.getPrice());
    order.setIdempotencyKey(UUID.randomUUID().toString());
    order = orderRepository.save(order);

    // Create order item
    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(testProduct);
    orderItem.setQuantity(1);
    orderItem.setPrice(testProduct.getPrice());
    orderItemRepository.save(orderItem);

    // Verify order is created with PROCESSING status
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);

    // Set order status to PENDING to allow payment
    order.setStatus(OrderStatus.PENDING);
    orderRepository.save(order);

    // Create payment DTO
    PaymentDTO paymentDTO = PaymentDTO.builder()
                                      .orderId(order.getId())
                                      .amount(order.getTotalAmount())
                                      .paymentMethod("Credit Card")
                                      .idempotencyKey(UUID.randomUUID().toString())
                                      .build();

    // Process payment
    PaymentDTO result = paymentService.processPayment(paymentDTO);

    // Verify payment is processed successfully
    assertThat(result).isNotNull();
    assertThat(result.getOrderId()).isEqualTo(order.getId());
    assertThat(result.getAmount()).isEqualTo(order.getTotalAmount());
    assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

    // Verify payment is saved in database
    Payment savedPayment = paymentRepository.findById(result.getId()).orElseThrow();
    assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    assertThat(savedPayment.getAmount()).isEqualTo(order.getTotalAmount());
    assertThat(savedPayment.getOrder().getId()).isEqualTo(order.getId());
  }

  @Test
  @DisplayName("Should return same payment when processing with same idempotency key")
  @WithMockUser(username = "testuser")
  void shouldReturnSamePaymentWhenProcessingWithSameIdempotencyKey() {
    // Create an order directly using repository
    Order order = new Order();
    order.setUser(testUser);
    order.setStatus(OrderStatus.PROCESSING);
    order.setTotalAmount(testProduct.getPrice());
    order.setIdempotencyKey(UUID.randomUUID().toString());
    order = orderRepository.save(order);

    // Create order item
    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(testProduct);
    orderItem.setQuantity(1);
    orderItem.setPrice(testProduct.getPrice());
    orderItemRepository.save(orderItem);

    // Set order status to PENDING to allow payment
    order.setStatus(OrderStatus.PENDING);
    orderRepository.save(order);

    // Create payment DTO with fixed idempotency key
    String idempotencyKey = UUID.randomUUID().toString();
    PaymentDTO paymentDTO = PaymentDTO.builder()
                                      .orderId(order.getId())
                                      .amount(order.getTotalAmount())
                                      .paymentMethod("Credit Card")
                                      .idempotencyKey(idempotencyKey)
                                      .build();

    // Process payment first time
    PaymentDTO result1 = paymentService.processPayment(paymentDTO);

    // Process payment second time with same idempotency key
    PaymentDTO result2 = paymentService.processPayment(paymentDTO);

    // Verify both results are the same
    assertThat(result2.getId()).isEqualTo(result1.getId());
    assertThat(result2.getStatus()).isEqualTo(result1.getStatus());

    // Verify only one payment is saved in database
    List<Payment> payments = paymentRepository.findAll();
    assertThat(payments).hasSize(1);
  }

  @Test
  @DisplayName("Should throw exception when user is not authorized")
  @WithMockUser(username = "testadmin")
    // Not an admin, just a user with a different username
  void shouldThrowExceptionWhenUserIsNotAuthorized() {
    // Create an order for testUser
    Order order = new Order();
    order.setUser(testUser); // Order belongs to testUser
    order.setStatus(OrderStatus.PENDING);
    order.setTotalAmount(testProduct.getPrice());
    order.setIdempotencyKey(UUID.randomUUID().toString());
    order = orderRepository.save(order);

    // Create order item
    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(testProduct);
    orderItem.setQuantity(1);
    orderItem.setPrice(testProduct.getPrice());
    orderItemRepository.save(orderItem);

    // Create payment DTO
    PaymentDTO paymentDTO = PaymentDTO.builder()
                                      .orderId(order.getId())
                                      .amount(order.getTotalAmount())
                                      .paymentMethod("Credit Card")
                                      .idempotencyKey(UUID.randomUUID().toString())
                                      .build();

    // Try to process payment as testadmin (without ROLE_ADMIN)
    assertThatThrownBy(() -> paymentService.processPayment(paymentDTO))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
  }


}
