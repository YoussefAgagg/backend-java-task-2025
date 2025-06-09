package com.gitthub.youssefagagg.ecommerceorderprocessor.service.impl;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.DailySalesReportDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.OrderItemDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaymentDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Inventory;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.NotificationType;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderItem;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.PaymentStatus;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.InventoryMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.OrderMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.InventoryRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.NotificationRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.OrderItemRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.OrderRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.ProductRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.AuthoritiesRole;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.SecurityUtils;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.AuditService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.BaseService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.NotificationService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.OrderService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.PaymentService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.WebSocketService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.util.KeyLockManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Order}.
 */
@Service
@Slf4j
public class OrderServiceImpl extends BaseService implements OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final InventoryRepository inventoryRepository;
  private final OrderItemRepository orderItemRepository;
  private final NotificationRepository notificationRepository;
  private final OrderMapper orderMapper;
  private final InventoryMapper inventoryMapper;
  private final WebSocketService webSocketService;
  private final AuditService auditService;
  private final PaymentService paymentService;
  private final NotificationService notificationService;
  private final KeyLockManager keyLockManager;

  public OrderServiceImpl(
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
      KeyLockManager keyLockManager) {
    super(userRepository);
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.inventoryRepository = inventoryRepository;
    this.orderItemRepository = orderItemRepository;
    this.notificationRepository = notificationRepository;
    this.orderMapper = orderMapper;
    this.inventoryMapper = inventoryMapper;
    this.webSocketService = webSocketService;
    this.auditService = auditService;
    this.paymentService = paymentService;
    this.notificationService = notificationService;
    this.keyLockManager = keyLockManager;
  }

  @Override
  @Transactional
  public OrderDTO createOrder(OrderDTO orderDTO) {
    log.debug("Request to create Order : {}", orderDTO);
    return keyLockManager.withLock(orderDTO.getIdempotencyKey(), () -> {
      // Step 1: Initialize order with current user
      Order order = initializeOrder(orderDTO);

      // Step 2: Validate and add order items
      order = addOrderItems(order, orderDTO.getOrderItems());

      // Step 3: Process payment
      order = processPayment(order);

      // Step 4: Update order status based on payment
      order = updateOrderAfterPayment(order);

      // Step 5: Send notifications and create audit logs
      notifyAndAudit(order);

      // Convert to DTO and send real-time updates
      OrderDTO result = orderMapper.toDto(order);
      webSocketService.sendOrderStatusUpdate(order.getUser().getId(), result);

      return result;
    });
  }

  /**
   * Initialize a new order with the current user and idempotency key if provided
   *
   * @param orderDTO the order DTO containing optional idempotency key
   * @return the initialized order
   */
  private Order initializeOrder(OrderDTO orderDTO) {
    log.debug("Initializing new order");
    User currentUser = getCurrentUser();

    Order order = new Order();
    order.setUser(currentUser);
    order.setStatus(OrderStatus.PENDING);
    order.setTotalAmount(BigDecimal.ZERO);
    order.setOrderItems(new ArrayList<>());
    order.setIdempotencyKey(orderDTO.getIdempotencyKey());

    return orderRepository.save(order);
  }

  /**
   * Add items to the order, check inventory, and calculate total
   */
  private Order addOrderItems(Order order, @Valid @NotEmpty List<OrderItemDTO> orderItemDTOs) {
    log.debug("Adding items to order and checking inventory");
    BigDecimal totalAmount = BigDecimal.ZERO;

    // Extract all product IDs from order items
    List<Long> productIds = orderItemDTOs.stream()
                                         .map(OrderItemDTO::getProductId)
                                         .toList();

    // Fetch all products with their inventory in a single call
    List<Product> products = productRepository.findByIdInWithInventory(productIds);

    // Create a map of product ID to product for easy lookup
    Map<Long, Product> productMap = products.stream()
                                            .collect(Collectors.toMap(Product::getId,
                                                                      Function.identity()));

    for (OrderItemDTO itemDTO : orderItemDTOs) {
      // Get product from the map
      Product product = productMap.get(itemDTO.getProductId());
      if (product == null) {
        throw new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                                  "Product not found: " + itemDTO.getProductId());
      }

      Inventory inventory = product.getInventory();

      // Check if enough inventory is available
      if (inventory.getAvailableQuantity() < itemDTO.getQuantity()) {
        throw new CustomException(ErrorCode.INVALID_REQUEST,
                                  "Not enough inventory for product: " + product.getName() +
                                  ". Available: " + inventory.getAvailableQuantity() +
                                  ", Requested: " + itemDTO.getQuantity());
      }

      // Reserve inventory
      if (!inventory.reserve(itemDTO.getQuantity())) {
        throw new CustomException(ErrorCode.INVALID_REQUEST,
                                  "Failed to reserve inventory for product: " + product.getName());
      }
      inventoryRepository.save(inventory);

      // Create and save order item
      OrderItem orderItem = createOrderItem(order, product, itemDTO.getQuantity());

      // Add to total amount
      totalAmount = totalAmount.add(orderItem.getSubtotal());
    }

    // Update order total
    order.setTotalAmount(totalAmount);
    return orderRepository.save(order);
  }

  /**
   * Create and save an order item
   */
  private OrderItem createOrderItem(Order order, Product product, Integer quantity) {
    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(product);
    orderItem.setQuantity(quantity);
    orderItem.setPrice(product.getPrice());

    OrderItem savedItem = orderItemRepository.save(orderItem);
    order.getOrderItems().add(savedItem);

    return savedItem;
  }

  /**
   * Process payment for an order
   */
  private Order processPayment(Order order) {
    log.debug("Processing payment for order: {}", order.getId());

    // Use the client-provided idempotency key if available, or generate one if not
    // This ensures that if this method is called multiple times for the same order
    // (e.g., due to network issues or retries), we won't process duplicate payments
    String idempotencyKey = order.getIdempotencyKey();
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      idempotencyKey = UUID.randomUUID().toString();
      log.debug("Generated idempotency key for order {}: {}", order.getId(), idempotencyKey);
    } else {
      log.debug("Using client-provided idempotency key for order {}: {}", order.getId(),
                idempotencyKey);
    }

    PaymentDTO paymentDTO = PaymentDTO.builder()
                                      .orderId(order.getId())
                                      .amount(order.getTotalAmount())
                                      .paymentMethod("Credit Card") // Default payment method
                                      .status(PaymentStatus.PENDING)
                                      .idempotencyKey(idempotencyKey) // Include the idempotency key
                                      .build();

    PaymentDTO processedPayment = paymentService.processPayment(paymentDTO);

    if (processedPayment.getStatus() == PaymentStatus.COMPLETED) {
      order.updateStatus(OrderStatus.PAID);
      order = orderRepository.save(order);
      log.debug("Payment successful, order status updated to PAID");
    } else {
      log.debug("Payment failed, order status remains PENDING");
    }

    return order;
  }

  /**
   * Update order status after payment processing
   */
  private Order updateOrderAfterPayment(Order order) {
    log.debug("Updating order status after payment");

    if (order.getStatus() == OrderStatus.PAID) {
      order.updateStatus(OrderStatus.PROCESSING);
      order = orderRepository.save(order);
      log.debug("Order is paid, updating status to PROCESSING");
    } else {
      log.debug("Order is not paid, status remains {}", order.getStatus());
    }

    return order;
  }

  /**
   * Send notifications and create audit logs
   */
  private void notifyAndAudit(Order order) {
    log.debug("Sending notifications and creating audit logs");

    // Create notification using NotificationService
    notificationService.createNotification(
        order.getUser(),
        NotificationType.ORDER_CONFIRMATION,
        "Your order #" + order.getId() + " has been confirmed."
                                          );

    // Create audit log asynchronously
    auditService.createLogAsync("Order", order.getId(), order);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse<OrderDTO> getCurrentUserOrders(Pageable pageable) {
    log.debug("Request to get current user orders");

    User currentUser = getCurrentUser();
    return findOrdersForUser(currentUser, null, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse<OrderDTO> getCurrentUserOrdersByStatus(OrderStatus status,
                                                                   Pageable pageable) {
    log.debug("Request to get current user orders by status: {}", status);

    User currentUser = getCurrentUser();
    return findOrdersForUser(currentUser, status, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse<OrderDTO> getAllOrders(Pageable pageable) {
    log.debug("Request to get all orders");

    return findAllOrders(null, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse<OrderDTO> getAllOrdersByStatus(OrderStatus status, Pageable pageable) {
    log.debug("Request to get all orders by status: {}", status);

    return findAllOrders(status, pageable);
  }

  /**
   * Find orders for a specific user, optionally filtered by status
   */
  private PaginationResponse<OrderDTO> findOrdersForUser(User user, OrderStatus status,
                                                         Pageable pageable) {
    Page<Order> result;

    if (status == null) {
      result = orderRepository.findByUser(user, pageable);
    } else {
      result = orderRepository.findByUserAndStatus(user, status, pageable);
    }

    Page<OrderDTO> dtoPage = result.map(orderMapper::toDto);
    return createPaginationResponse(dtoPage);
  }

  /**
   * Find all orders, optionally filtered by status
   */
  private PaginationResponse<OrderDTO> findAllOrders(OrderStatus status, Pageable pageable) {
    Page<Order> result;

    if (status == null) {
      result = orderRepository.findAll(pageable);
    } else {
      result = orderRepository.findByStatus(status, pageable);
    }

    Page<OrderDTO> dtoPage = result.map(orderMapper::toDto);
    return createPaginationResponse(dtoPage);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderDTO getOrder(Long id) {
    log.debug("Request to get Order : {}", id);

    // Get order and verify authorization
    Order order = findOrderAndVerifyAccess(id);
    return orderMapper.toDto(order);
  }

  @Override
  @Transactional
  public OrderDTO cancelOrder(Long id) {
    log.debug("Request to cancel Order : {}", id);

    // Get order and verify authorization
    Order order = findOrderAndVerifyAccess(id);

    // Check if order can be cancelled
    if (order.getStatus() != OrderStatus.PENDING
        && order.getStatus() != OrderStatus.PAID
        && order.getStatus() != OrderStatus.PROCESSING) {
      throw new CustomException(ErrorCode.INVALID_REQUEST,
                                "Cannot cancel order with status: " + order.getStatus());
    }

    // Update order status to CANCELLED
    OrderStatus oldStatus = order.getStatus();
    order.setStatus(OrderStatus.CANCELLED);
    // Save updated order
    order = orderRepository.save(order);

    // Release reserved inventory
    releaseInventoryForOrder(order);

    // Create notification and audit log
    createCancellationNotification(order);
    auditOrderStatusChange(order, oldStatus);

    // Convert to DTO and send real-time updates
    OrderDTO result = orderMapper.toDto(order);
    log.info("sending cancellation notification for order: {}", order.getId());
    sendStatusUpdateNotifications(order, oldStatus, result);

    return result;
  }

  /**
   * Find an order by ID and verify the current user has access to it
   */
  private Order findOrderAndVerifyAccess(Long id) {
    String currentUser = SecurityUtils.getCurrentUserUserName().orElseThrow();
    Order order = orderRepository.findById(id)
                                 .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                                                                        "Order not found"));

    boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(
        AuthoritiesRole.ROLE_ADMIN.getValue());

    if (!isAdmin && !order.getUser().getUsername().equals(currentUser)) {
      throw new CustomException(ErrorCode.ACCESS_DENIED, "Not authorized to access this order");
    }

    return order;
  }

  /**
   * Update an order's status and save it
   */
  private Order updateOrderStatus(Order order, OrderStatus newStatus) {
    order.updateStatus(newStatus);
    return orderRepository.save(order);
  }

  /**
   * Release reserved inventory for all items in an order
   */
  private void releaseInventoryForOrder(Order order) {
    for (OrderItem item : order.getOrderItems()) {
      Inventory inventory = inventoryRepository.findByProduct(item.getProduct())
                                               .orElseThrow(() -> new CustomException(
                                                   ErrorCode.ENTITY_NOT_FOUND,
                                                   "Inventory not found for product: " + item
                                                       .getProduct().getId()));

      inventory.releaseReservation(item.getQuantity());
      inventoryRepository.save(inventory);

      // Send inventory update
      webSocketService.sendInventoryUpdate(inventoryMapper.toDto(inventory));
    }
  }

  /**
   * Create a cancellation notification for an order
   */
  private void createCancellationNotification(Order order) {
    notificationService.createNotification(
        order.getUser(),
        NotificationType.ORDER_CONFIRMATION,
        "Your order #" + order.getId() + " has been cancelled."
                                          );
  }

  /**
   * Create an audit log for an order status change
   */
  private void auditOrderStatusChange(Order order, OrderStatus oldStatus) {
    Order oldOrder = order.cloneObject();
    oldOrder.setStatus(oldStatus);
    auditService.updateLogAsync("Order", order.getId(),
                                "Status changed from " + oldStatus + " to " + order.getStatus(),
                                oldOrder, order);
  }

  /**
   * Send real-time status update notifications
   */
  private void sendStatusUpdateNotifications(Order order, OrderStatus oldStatus,
                                             OrderDTO orderDTO) {
    log.info("sending status update notifications for order: {}", order.getId());
    webSocketService.sendOrderStatusUpdate(order.getUser().getId(), orderDTO);
    log.info("sending admin order status change event for order: {}", order.getId());
    webSocketService.sendOrderStatusChangeEvent(order.getId(), oldStatus, order.getStatus());
  }

  @Override
  @Transactional
  public OrderDTO updateOrderStatus(Long id, OrderStatus status) {
    log.debug("Request to update Order status : {}, {}", id, status);

    // Get order (admin only operation, no need to check user access)
    Order order = orderRepository.findById(id)
                                 .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                                                                        "Order not found"));

    // Get current status before update
    OrderStatus oldStatus = order.getStatus();

    // Update order status
    order.updateStatus(status);

    // If status didn't change, return early
    if (oldStatus == order.getStatus()) {
      return orderMapper.toDto(order);
    }

    // Process status-specific actions
    processStatusSpecificActions(order, status);

    // Save updated order
    order = orderRepository.save(order);

    // Create audit log
    auditOrderStatusChange(order, oldStatus);

    // Convert to DTO and send real-time updates
    OrderDTO result = orderMapper.toDto(order);
    sendStatusUpdateNotifications(order, oldStatus, result);

    return result;
  }

  /**
   * Process actions specific to each order status
   */
  private void processStatusSpecificActions(Order order, OrderStatus status) {
    switch (status) {
      case PAID:
        // Process payment confirmation logic would go here
        break;
      case PROCESSING:
        // Process order fulfillment logic would go here
        break;
      case SHIPPED:
        createShippingNotification(order);
        break;
      case DELIVERED:
        fulfillInventoryForOrder(order);
        break;
      case CANCELLED:
        releaseInventoryForOrder(order);
        break;
      default:
        break;
    }
  }

  /**
   * Create a shipping notification for an order
   */
  private void createShippingNotification(Order order) {
    notificationService.createNotification(
        order.getUser(),
        NotificationType.SHIPPING_UPDATE,
        "Order #" + order.getId() + " has been shipped."
                                          );
  }

  /**
   * Fulfill inventory for all items in an order (reduce reserved quantity)
   */
  private void fulfillInventoryForOrder(Order order) {
    for (OrderItem item : order.getOrderItems()) {
      Inventory inventory = inventoryRepository.findByProduct(item.getProduct())
                                               .orElseThrow(() -> new CustomException(
                                                   ErrorCode.ENTITY_NOT_FOUND,
                                                   "Inventory not found for product: " + item
                                                       .getProduct().getId()));

      inventory.fulfill(item.getQuantity());
      inventoryRepository.save(inventory);

      // Send inventory update
      webSocketService.sendInventoryUpdate(inventoryMapper.toDto(inventory));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public OrderStatus getOrderStatus(Long id) {
    log.debug("Request to get Order status : {}", id);

    return orderRepository.findById(id)
                          .map(Order::getStatus)
                          .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                                                                 "Order not found"));
  }

  @Transactional(readOnly = true)
  @Override
  public List<DailySalesReportDTO> getDailySalesReport(LocalDate startDate, LocalDate endDate) {
    log.debug("Service request to get daily sales report from {} to {}", startDate, endDate);

    Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

    List<Object[]> results = orderRepository.getDailySalesReport(startInstant, endInstant);

    return results.stream()
                  .map(row -> {
                    LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                    BigDecimal totalSales = (BigDecimal) row[1];
                    return new DailySalesReportDTO(date, totalSales);
                  })
                  .collect(Collectors.toList());
  }

}
