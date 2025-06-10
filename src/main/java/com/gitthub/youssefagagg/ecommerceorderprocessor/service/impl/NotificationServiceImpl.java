package com.gitthub.youssefagagg.ecommerceorderprocessor.service.impl;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.NotificationDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Notification;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.NotificationType;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.NotificationMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.NotificationRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.security.SecurityUtils;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.BaseService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.NotificationService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Notification}.
 */
@Service
@Slf4j
public class NotificationServiceImpl extends BaseService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final WebSocketService webSocketService;

  public NotificationServiceImpl(
      UserRepository userRepository,
      NotificationRepository notificationRepository,
      NotificationMapper notificationMapper,
      WebSocketService webSocketService) {
    super(userRepository);
    this.notificationRepository = notificationRepository;
    this.notificationMapper = notificationMapper;
    this.webSocketService = webSocketService;
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse<NotificationDTO> getCurrentUserNotifications(Pageable pageable) {
    log.debug("Request to get current user notifications");

    // Get current user
    User currentUser = getCurrentUser();

    Page<Notification> result = notificationRepository.findByUser(currentUser, pageable);
    Page<NotificationDTO> dtoPage = result.map(notificationMapper::toDto);

    return createPaginationResponse(dtoPage);
  }



  @Override
  @Transactional
  public NotificationDTO markAsRead(Long id) {
    log.debug("Request to mark Notification as read : {}", id);

    Notification notification = notificationRepository.findById(id)
                                                      .orElseThrow(() -> new CustomException(
                                                          ErrorCode.ENTITY_NOT_FOUND,
                                                          "Notification not found"));

    // Check if user is authorized to mark this notification as read
    String currentUser = SecurityUtils.getCurrentUserUserName().orElseThrow();

    if (!notification.getUser().getUsername().equals(currentUser)) {
      throw new CustomException(ErrorCode.ACCESS_DENIED,
                                "Not authorized to mark this notification as read");
    }

    notification.setIsRead(true);
    notification = notificationRepository.save(notification);

    // Convert to DTO
    NotificationDTO notificationDTO = notificationMapper.toDto(notification);

    // Send real-time update
    webSocketService.sendNotification(notification.getUser().getUsername(), notificationDTO);

    return notificationDTO;
  }

  @Override
  @Transactional
  public int markAllAsRead() {
    log.debug("Request to mark all notifications as read for current user");

    // Get current user
    User currentUser = getCurrentUser();
    // Mark all as read
    int count = notificationRepository.markAllAsRead(currentUser);

    // Flush to ensure changes are committed to the database
    notificationRepository.flush();

    if (count > 0) {
      // Also send an update for the unread count (which is now 0)
      webSocketService.sendNotification(currentUser.getUsername(),
                                        createCountNotification(currentUser.getId(), 0L));
    }


    return count;
  }

  /**
   * Create a notification for a user.
   *
   * @param user    the user
   * @param type    the notification type
   * @param content the notification content
   * @return the created notification DTO
   */
  @Transactional
  @Async("taskExecutor")
  @Override
  public void createNotification(User user, NotificationType type, String content) {
    log.debug("Creating notification for user ID: {}, type: {}", user.getId(), type);

    Notification notification = Notification.builder()
                                            .user(user)
                                            .type(type)
                                            .content(content)
                                            .isRead(false)
                                            .build();

    notification = notificationRepository.save(notification);

    // Convert to DTO
    NotificationDTO notificationDTO = notificationMapper.toDto(notification);

    // Send real-time update
    webSocketService.sendNotification(user.getUsername(), notificationDTO);

    // Also send an update for the unread count
    long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);
    webSocketService.sendNotification(user.getUsername(),
                                      createCountNotification(user.getId(), unreadCount));

  }

  /**
   * Create a special notification DTO for unread count updates.
   *
   * @param userId the user ID
   * @param count  the unread count
   * @return the notification DTO
   */
  private NotificationDTO createCountNotification(Long userId, Long count) {
    return NotificationDTO.builder()
                          .userId(userId)
                          .type(NotificationType.UNREAD_COUNT)
                          .content(count.toString())
                          .isRead(true)
                          .build();
  }

  @Override
  @Transactional(readOnly = true)
  public long countUnreadNotifications() {
    log.debug("Request to count unread notifications for current user");

    // Get current user
    User currentUser = getCurrentUser();

    return notificationRepository.countByUserAndIsReadFalse(currentUser);
  }

}
