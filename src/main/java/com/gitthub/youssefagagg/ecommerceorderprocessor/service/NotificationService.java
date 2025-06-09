package com.gitthub.youssefagagg.ecommerceorderprocessor.service;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.NotificationDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Notification;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.NotificationType;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Interface for managing
 * {@link Notification}.
 */
public interface NotificationService {

  /**
   * Get all notifications for the current user with pagination.
   *
   * @param pageable the pagination information
   * @return the list of notifications
   */
  PaginationResponse<NotificationDTO> getCurrentUserNotifications(Pageable pageable);

  /**
   * Get all unread notifications for the current user with pagination.
   *
   * @param pageable the pagination information
   * @return the list of unread notifications
   */
  PaginationResponse<NotificationDTO> getCurrentUserUnreadNotifications(Pageable pageable);

  /**
   * Get all notifications for the current user with the given type and pagination.
   *
   * @param type     the notification type
   * @param pageable the pagination information
   * @return the list of notifications
   */
  PaginationResponse<NotificationDTO> getCurrentUserNotificationsByType(NotificationType type,
                                                                        Pageable pageable);

  /**
   * Get a notification by ID.
   *
   * @param id the notification ID
   * @return the notification
   */
  NotificationDTO getNotification(Long id);

  /**
   * Mark a notification as read.
   *
   * @param id the notification ID
   * @return the updated notification
   */
  NotificationDTO markAsRead(Long id);

  /**
   * Mark all notifications for the current user as read.
   *
   * @return the number of notifications marked as read
   */
  int markAllAsRead();

  @Transactional
  void createNotification(User user, NotificationType type, String content);

  /**
   * Count unread notifications for the current user.
   *
   * @return the count of unread notifications
   */
  long countUnreadNotifications();
}