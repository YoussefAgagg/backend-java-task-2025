package com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Role;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.PaymentDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.NotificationType;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.OrderStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Payment;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.PaymentStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper.PaymentMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.NotificationRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.OrderRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.PaymentRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.AuditService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.NotificationService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.AuthoritiesRole;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.SecurityUtils;
import java.math.BigDecimal;
import java.util.HashSet;
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

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

  private MockedStatic<SecurityUtils> securityUtilsMock;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private PaymentMapper paymentMapper;

  @Mock
  private AuditService auditService;

  @Mock
  private NotificationService notificationService;

  @Mock
  private UserRepository userRepository;

  private TestPaymentServiceImpl paymentService;

  private User user;
  private User adminUser;
  private Order order;
  private Payment payment;
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

    // Setup order
    order = new Order();
    order.setId(1L);
    order.setUser(user);
    order.setStatus(OrderStatus.PENDING);
    order.setTotalAmount(BigDecimal.valueOf(99.99));

    // Setup payment
    payment = new Payment();
    payment.setId(1L);
    payment.setOrder(order);
    payment.setAmount(BigDecimal.valueOf(99.99));
    payment.setPaymentMethod("Credit Card");
    payment.setStatus(PaymentStatus.COMPLETED);
    payment.setTransactionId(UUID.randomUUID().toString());
    payment.setIdempotencyKey(UUID.randomUUID().toString());

    // Setup payment DTO
    paymentDTO = PaymentDTO.builder()
                           .id(1L)
                           .orderId(order.getId())
                           .amount(order.getTotalAmount())
                           .paymentMethod("Credit Card")
                           .status(PaymentStatus.PENDING)
                           .idempotencyKey(UUID.randomUUID().toString())
                           .build();

    // Create service with test user
    paymentService = new TestPaymentServiceImpl(
        userRepository,
        paymentRepository,
        orderRepository,
        notificationRepository,
        paymentMapper,
        auditService,
        notificationService,
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
  @DisplayName("Should process payment successfully")
  void shouldProcessPaymentSuccessfully() {
    // Given
    when(orderRepository.findById(paymentDTO.getOrderId())).thenReturn(Optional.of(order));
    when(paymentRepository.findByIdempotencyKey(paymentDTO.getIdempotencyKey())).thenReturn(
        Optional.empty());
    when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
      Payment savedPayment = invocation.getArgument(0);
      savedPayment.setId(1L);
      return savedPayment;
    });
    when(paymentMapper.toDto(any(Payment.class))).thenReturn(paymentDTO);

    // When
    PaymentDTO result = paymentService.processPayment(paymentDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(paymentDTO.getId());
    assertThat(result.getOrderId()).isEqualTo(paymentDTO.getOrderId());
    assertThat(result.getAmount()).isEqualTo(paymentDTO.getAmount());

    verify(orderRepository).findById(paymentDTO.getOrderId());
    verify(paymentRepository).findByIdempotencyKey(paymentDTO.getIdempotencyKey());
    verify(paymentRepository, times(2)).save(any(Payment.class));
    verify(notificationService).createNotification(any(User.class),
                                                   eq(NotificationType.PAYMENT_CONFIRMATION),
                                                   anyString());
    verify(auditService).createLogAsync(eq("Payment"), anyLong(), any(Payment.class));
  }

  @Test
  @DisplayName("Should return existing payment when idempotency key exists with completed status")
  void shouldReturnExistingPaymentWhenIdempotencyKeyExistsWithCompletedStatus() {
    // Given
    payment.setStatus(PaymentStatus.COMPLETED);
    when(orderRepository.findById(paymentDTO.getOrderId())).thenReturn(Optional.of(order));
    when(paymentRepository.findByIdempotencyKey(paymentDTO.getIdempotencyKey())).thenReturn(
        Optional.of(payment));
    when(paymentMapper.toDto(payment)).thenReturn(paymentDTO);

    // When
    PaymentDTO result = paymentService.processPayment(paymentDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(paymentDTO.getId());

    verify(orderRepository).findById(paymentDTO.getOrderId());
    verify(paymentRepository).findByIdempotencyKey(paymentDTO.getIdempotencyKey());
    verify(paymentRepository, never()).save(any(Payment.class));
    verify(notificationService, never()).createNotification(any(User.class), any(), anyString());
    verify(auditService, never()).createLogAsync(anyString(), anyLong(), any());
  }

  @Test
  @DisplayName("Should throw exception when order not found")
  void shouldThrowExceptionWhenOrderNotFound() {
    // Given
    when(orderRepository.findById(paymentDTO.getOrderId())).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> paymentService.processPayment(paymentDTO))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(orderRepository).findById(paymentDTO.getOrderId());
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  @DisplayName("Should throw exception when user is not authorized")
  void shouldThrowExceptionWhenUserIsNotAuthorized() {
    // Given
    User otherUser = new User();
    otherUser.setId(3L);
    otherUser.setUsername("otheruser");
    order.setUser(otherUser);

    when(orderRepository.findById(paymentDTO.getOrderId())).thenReturn(Optional.of(order));

    // When/Then
    assertThatThrownBy(() -> paymentService.processPayment(paymentDTO))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);

    verify(orderRepository).findById(paymentDTO.getOrderId());
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  @DisplayName("Should allow admin to process payment for any order")
  void shouldAllowAdminToProcessPaymentForAnyOrder() {
    // Given
    // Mock admin privileges
    securityUtilsMock.when(
                         () -> SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesRole.ROLE_ADMIN.getValue()))
                     .thenReturn(true);

    User otherUser = new User();
    otherUser.setId(3L);
    order.setUser(otherUser);

    // Create service with admin user
    paymentService = new TestPaymentServiceImpl(
        userRepository,
        paymentRepository,
        orderRepository,
        notificationRepository,
        paymentMapper,
        auditService,
        notificationService,
        adminUser
    );

    when(orderRepository.findById(paymentDTO.getOrderId())).thenReturn(Optional.of(order));
    when(paymentRepository.findByIdempotencyKey(paymentDTO.getIdempotencyKey())).thenReturn(
        Optional.empty());
    when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
      Payment savedPayment = invocation.getArgument(0);
      savedPayment.setId(1L);
      return savedPayment;
    });
    when(paymentMapper.toDto(any(Payment.class))).thenReturn(paymentDTO);

    // When
    PaymentDTO result = paymentService.processPayment(paymentDTO);

    // Then
    assertThat(result).isNotNull();

    verify(orderRepository).findById(paymentDTO.getOrderId());
    verify(paymentRepository).findByIdempotencyKey(paymentDTO.getIdempotencyKey());
    verify(paymentRepository, times(2)).save(any(Payment.class));
  }

  @Test
  @DisplayName("Should throw exception when order status is not PENDING")
  void shouldThrowExceptionWhenOrderStatusIsNotPending() {
    // Given
    order.setStatus(OrderStatus.PROCESSING);
    when(orderRepository.findById(paymentDTO.getOrderId())).thenReturn(Optional.of(order));

    // When/Then
    assertThatThrownBy(() -> paymentService.processPayment(paymentDTO))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

    verify(orderRepository).findById(paymentDTO.getOrderId());
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  @Test
  @DisplayName("Should throw exception when payment amount does not match order total")
  void shouldThrowExceptionWhenPaymentAmountDoesNotMatchOrderTotal() {
    // Given
    paymentDTO.setAmount(BigDecimal.valueOf(50.00)); // Different from order total
    when(orderRepository.findById(paymentDTO.getOrderId())).thenReturn(Optional.of(order));

    // When/Then
    assertThatThrownBy(() -> paymentService.processPayment(paymentDTO))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);

    verify(orderRepository).findById(paymentDTO.getOrderId());
    verify(paymentRepository, never()).save(any(Payment.class));
  }

  // Test subclass that overrides getCurrentUser to avoid static mocking
  private class TestPaymentServiceImpl extends PaymentServiceImpl {
    private final User currentUser;

    public TestPaymentServiceImpl(
        UserRepository userRepository,
        PaymentRepository paymentRepository,
        OrderRepository orderRepository,
        NotificationRepository notificationRepository,
        PaymentMapper paymentMapper,
        AuditService auditService,
        NotificationService notificationService,
        User currentUser) {
      super(userRepository, paymentRepository, orderRepository, notificationRepository,
            paymentMapper, auditService, notificationService);
      this.currentUser = currentUser;
    }

    @Override
    protected User getCurrentUser() {
      return currentUser;
    }
  }
}
