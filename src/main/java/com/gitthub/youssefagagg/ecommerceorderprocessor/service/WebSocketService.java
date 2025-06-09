package com.gitthub.youssefagagg.ecommerceorderprocessor.service;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.InventoryDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.NotificationDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending real-time updates via WebSocket.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {

  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Send inventory update to all connected clients.
   *
   * @param inventoryDTO the updated inventory
   */
  public void sendInventoryUpdate(InventoryDTO inventoryDTO) {
    log.debug("Sending inventory update for product ID: {}", inventoryDTO.getProductId());
    messagingTemplate.convertAndSend("/topic/inventory", inventoryDTO);
  }

  /**
   * Send order status update to specific user.
   *
   * @param userId   the user ID
   * @param orderDTO the updated order
   */
  public void sendOrderStatusUpdate(Long userId, OrderDTO orderDTO) {
    log.debug("Sending order status update for order ID: {} to user ID: {}", orderDTO.getId(),
              userId);
    messagingTemplate.convertAndSend("/topic/orders/" + userId, orderDTO);
  }

  /**
   * Send notification to specific user.
   *
   * @param userId          the user ID
   * @param notificationDTO the notification
   */
  public void sendNotification(Long userId, NotificationDTO notificationDTO) {
    log.debug("Sending notification to user ID: {}", userId);
    messagingTemplate.convertAndSend("/topic/notifications/" + userId, notificationDTO);
  }

  /**
   * Send order status change event to admin dashboard.
   *
   * @param orderId   the order ID
   * @param oldStatus the old status
   * @param newStatus the new status
   */
  public void sendOrderStatusChangeEvent(Long orderId, OrderStatus oldStatus,
                                         OrderStatus newStatus) {
    log.debug("Sending order status change event for order ID: {}", orderId);
    messagingTemplate.convertAndSend("/topic/admin/orders/status",
                                     new OrderStatusChangeEvent(orderId, oldStatus, newStatus));
  }

  /**
   * Send low stock alert to admin dashboard.
   *
   * @param inventoryDTO the low stock inventory
   */
  public void sendLowStockAlert(InventoryDTO inventoryDTO) {
    log.debug("Sending low stock alert for product ID: {}", inventoryDTO.getProductId());
    messagingTemplate.convertAndSend("/topic/admin/inventory/low-stock", inventoryDTO);
  }

  /**
   * Event class for order status changes.
   */
  public record OrderStatusChangeEvent(Long orderId,
                                       OrderStatus oldStatus,
                                       OrderStatus newStatus) {}
}