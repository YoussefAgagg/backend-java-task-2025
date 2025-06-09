package com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.impl;

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
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.PaymentService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.AuthoritiesRole;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.SecurityUtils;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.BaseService;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Payment}.
 */
@Service
@Slf4j
public class PaymentServiceImpl extends BaseService implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final NotificationRepository notificationRepository;
  private final PaymentMapper paymentMapper;
  private final AuditService auditService;
  private final NotificationService notificationService;

  public PaymentServiceImpl(
      UserRepository userRepository,
      PaymentRepository paymentRepository,
      OrderRepository orderRepository,
      NotificationRepository notificationRepository,
      PaymentMapper paymentMapper,
      AuditService auditService,
      NotificationService notificationService) {
    super(userRepository);
    this.paymentRepository = paymentRepository;
    this.orderRepository = orderRepository;
    this.notificationRepository = notificationRepository;
    this.paymentMapper = paymentMapper;
    this.auditService = auditService;
    this.notificationService = notificationService;
  }

  @Override
  @Transactional
  public PaymentDTO processPayment(PaymentDTO paymentDTO) {
    log.debug("Request to process Payment : {}", paymentDTO);

    // Validate payment data
    validatePaymentRequest(paymentDTO);

    // Check if order exists
    Order order = orderRepository.findById(paymentDTO.getOrderId())
                                 .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                                                                        "Order not found"));

    // Check if user is authorized to make payment for this order
    String currentUser = SecurityUtils.getCurrentUserUserName().orElseThrow();

    boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(
        AuthoritiesRole.ROLE_ADMIN.getValue());

    if (!isAdmin && !order.getUser().getUsername().equals(currentUser)) {
      throw new CustomException(ErrorCode.ACCESS_DENIED,
                                "Not authorized to make payment for this order");
    }

    // Check if order is in a state that allows payment
    if (order.getStatus() != OrderStatus.PENDING) {
      throw new CustomException(ErrorCode.INVALID_REQUEST,
                                "Cannot process payment for order with status: "
                                + order.getStatus());
    }

    // Check if payment amount matches order total
    if (paymentDTO.getAmount().compareTo(order.getTotalAmount()) != 0) {
      throw new CustomException(ErrorCode.INVALID_REQUEST,
                                "Payment amount does not match order total. Expected: "
                                + order.getTotalAmount() +
                                ", Actual: " + paymentDTO.getAmount());
    }

    String idempotencyKey = paymentDTO.getIdempotencyKey();

    // Check if payment with this idempotency key already exists
    Optional<Payment> existingPaymentByIdempotencyKey = paymentRepository.findByIdempotencyKey(
        idempotencyKey);
    if (existingPaymentByIdempotencyKey.isPresent()) {
      Payment existingPayment = existingPaymentByIdempotencyKey.get();
      log.info("Payment with idempotency key {} already exists with status {}",
               idempotencyKey, existingPayment.getStatus());

      // Return the existing payment regardless of its status
      // This ensures idempotency - same request always returns same result
      if (existingPayment.getStatus() == PaymentStatus.COMPLETED) {
        log.info("Returning existing completed payment with idempotency key {}", idempotencyKey);
        return paymentMapper.toDto(existingPayment);
      }
    }


    // Create or update payment entity
    Payment payment;
    if (existingPaymentByIdempotencyKey.isPresent()) {
      payment = existingPaymentByIdempotencyKey.get();
    } else {
      payment = new Payment();
      payment.setOrder(order);
      payment.setAmount(paymentDTO.getAmount());
      payment.setPaymentMethod(paymentDTO.getPaymentMethod());
      payment.setStatus(PaymentStatus.PENDING);
    }

    payment.setTransactionId(UUID.randomUUID().toString());
    payment.setIdempotencyKey(idempotencyKey);
    payment = paymentRepository.save(payment);

    // Process payment (in a real system, this would call a payment gateway)
    // Simulate payment processing
    boolean success = processPayment(payment);

    if (success) {
      // Payment succeeded
      log.info("Payment succeeded for order ID: {}", order.getId());
      payment.setStatus(PaymentStatus.COMPLETED);
      // Create notification using NotificationService
      notificationService.createNotification(
          order.getUser(),
          NotificationType.PAYMENT_CONFIRMATION,
          "Payment for order #" + order.getId() + " has been confirmed."
                                            );

      // Create audit logs asynchronously
      auditService.createLogAsync("Payment", payment.getId(), payment);
    } else {
      // Payment failed
      log.info("Payment failed for order ID: {}", order.getId());
      payment.setStatus(PaymentStatus.FAILED);
    }
    payment = paymentRepository.save(payment);

    return paymentMapper.toDto(payment);
  }

  private void validatePaymentRequest(PaymentDTO paymentDTO) {
    if (paymentDTO.getOrderId() == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST, "Order ID must not be null");
    }

    if (paymentDTO.getAmount() == null || paymentDTO.getAmount().signum() <= 0) {
      throw new CustomException(ErrorCode.INVALID_REQUEST, "Amount must be positive");
    }

    if (paymentDTO.getPaymentMethod() == null || paymentDTO.getPaymentMethod().isBlank()) {
      throw new CustomException(ErrorCode.INVALID_REQUEST, "Payment method must not be empty");
    }
  }

  private boolean processPayment(Payment payment) {
    // If payment is already completed with this transaction ID, return success
    if (payment.getStatus() == PaymentStatus.COMPLETED) {
      log.info("Payment with transaction ID {} is already completed", payment.getTransactionId());
      return true;
    }

    // Process payment logic would go here

    return true;
  }


}
