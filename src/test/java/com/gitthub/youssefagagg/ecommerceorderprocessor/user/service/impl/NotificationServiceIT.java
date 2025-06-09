package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gitthub.youssefagagg.ecommerceorderprocessor.TestcontainersConfiguration;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.NotificationDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Notification;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.NotificationType;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.NotificationRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class NotificationServiceIT {

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private UserRepository userRepository;

  private User testUser;
  private User otherUser;
  private Notification notification;

  @BeforeEach
  void setUp() {
    // Clean up any existing data
    notificationRepository.deleteAll();

    // Create test user
    testUser = new User();
    testUser.setUsername("testuser");
    testUser.setEmail("testuser@example.com");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    // Password must be exactly 60 characters long
    testUser.setPassword("$2a$10$eDhncK/4cNH2KE.Y51AWpeL8/5TCTWBpxmVhvZuLfCPIb4SLQtEP6");
    testUser = userRepository.save(testUser);

    // Create another test user
    otherUser = new User();
    otherUser.setUsername("otheruser");
    otherUser.setEmail("otheruser@example.com");
    otherUser.setFirstName("Other");
    otherUser.setLastName("User");
    // Password must be exactly 60 characters long
    otherUser.setPassword("$2a$10$eDhncK/4cNH2KE.Y51AWpeL8/5TCTWBpxmVhvZuLfCPIb4SLQtEP6");
    otherUser = userRepository.save(otherUser);

    // Create test notification
    notification = new Notification();
    notification.setUser(testUser);
    notification.setType(NotificationType.ORDER_CONFIRMATION);
    notification.setContent("Your order has been confirmed");
    notification.setIsRead(false);
    notification = notificationRepository.save(notification);
  }

  @Test
  @DisplayName("Should get current user notifications")
  @WithMockUser(username = "testuser")
  void shouldGetCurrentUserNotifications() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);

    // When
    PaginationResponse<NotificationDTO> result = notificationService.getCurrentUserNotifications(
        pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).isNotEmpty();
    assertThat(result.data().get(0).getId()).isEqualTo(notification.getId());
    assertThat(result.data().get(0).getUserId()).isEqualTo(testUser.getId());
    assertThat(result.data().get(0).getType()).isEqualTo(NotificationType.ORDER_CONFIRMATION);
    assertThat(result.data().get(0).getContent()).isEqualTo("Your order has been confirmed");
    assertThat(result.data().get(0).getIsRead()).isFalse();
  }

  @Test
  @DisplayName("Should get current user unread notifications")
  @WithMockUser(username = "testuser")
  void shouldGetCurrentUserUnreadNotifications() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);

    // When
    PaginationResponse<NotificationDTO> result =
        notificationService.getCurrentUserUnreadNotifications(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).isNotEmpty();
    assertThat(result.data().get(0).getId()).isEqualTo(notification.getId());
    assertThat(result.data().get(0).getIsRead()).isFalse();
  }

  @Test
  @DisplayName("Should get current user notifications by type")
  @WithMockUser(username = "testuser")
  void shouldGetCurrentUserNotificationsByType() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    NotificationType type = NotificationType.ORDER_CONFIRMATION;

    // When
    PaginationResponse<NotificationDTO> result =
        notificationService.getCurrentUserNotificationsByType(type, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).isNotEmpty();
    assertThat(result.data().get(0).getId()).isEqualTo(notification.getId());
    assertThat(result.data().get(0).getType()).isEqualTo(type);
  }

  @Test
  @DisplayName("Should get notification by ID")
  @WithMockUser(username = "testuser")
  void shouldGetNotificationById() {
    // When
    NotificationDTO result = notificationService.getNotification(notification.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(notification.getId());
    assertThat(result.getUserId()).isEqualTo(testUser.getId());
    assertThat(result.getType()).isEqualTo(notification.getType());
    assertThat(result.getContent()).isEqualTo(notification.getContent());
  }

  @Test
  @DisplayName("Should throw exception when getting non-existent notification")
  @WithMockUser(username = "testuser")
  void shouldThrowExceptionWhenGettingNonExistentNotification() {
    // Given
    Long nonExistentId = 999L;

    // When/Then
    assertThatThrownBy(() -> notificationService.getNotification(nonExistentId))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
  }

  @Test
  @DisplayName("Should throw exception when getting notification belonging to another user")
  @WithMockUser(username = "otheruser")
  void shouldThrowExceptionWhenGettingNotificationBelongingToAnotherUser() {
    // When/Then
    assertThatThrownBy(() -> notificationService.getNotification(notification.getId()))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
  }

  @Test
  @DisplayName("Should mark notification as read")
  @WithMockUser(username = "testuser")
  void shouldMarkNotificationAsRead() {
    // When
    NotificationDTO result = notificationService.markAsRead(notification.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(notification.getId());
    assertThat(result.getIsRead()).isTrue();

    // Verify notification is updated in database
    Notification updatedNotification = notificationRepository.findById(notification.getId())
                                                             .orElseThrow();
    assertThat(updatedNotification.getIsRead()).isTrue();
  }

  @Test
  @DisplayName("Should mark all notifications as read")
  @WithMockUser(username = "testuser")
  void shouldMarkAllNotificationsAsRead() {
    // Given
    // Create a few more unread notifications
    for (int i = 0; i < 3; i++) {
      Notification newNotification = new Notification();
      newNotification.setUser(testUser);
      newNotification.setType(NotificationType.SHIPPING_UPDATE);
      newNotification.setContent("Your order has been shipped");
      newNotification.setIsRead(false);
      notificationRepository.save(newNotification);
    }

    // When
    int result = notificationService.markAllAsRead();

    // Then
    assertThat(result).isEqualTo(4); // 1 original + 3 new notifications

    // Verify all notifications are marked as read in database
    Page<Notification> notificationsPage = notificationRepository.findByUser(testUser,
                                                                             Pageable.unpaged());
    List<Notification> notifications = notificationsPage.getContent();
    assertThat(notifications).allMatch(n -> n.getIsRead());
  }

  // Note: createNotification is tested in the unit test
  // We skip testing it in the integration test because it's @Async
  // and causes transaction issues in the test environment

  @Test
  @DisplayName("Should count unread notifications")
  @WithMockUser(username = "testuser")
  void shouldCountUnreadNotifications() {
    // Given
    // Create a few more unread notifications
    for (int i = 0; i < 3; i++) {
      Notification newNotification = new Notification();
      newNotification.setUser(testUser);
      newNotification.setType(NotificationType.SHIPPING_UPDATE);
      newNotification.setContent("Your order has been shipped");
      newNotification.setIsRead(false);
      notificationRepository.save(newNotification);
    }

    // When
    long result = notificationService.countUnreadNotifications();

    // Then
    assertThat(result).isEqualTo(4); // 1 original + 3 new notifications
  }
}
