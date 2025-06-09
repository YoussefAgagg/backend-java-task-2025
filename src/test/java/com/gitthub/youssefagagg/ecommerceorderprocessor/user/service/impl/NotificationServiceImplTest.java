package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.NotificationDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Notification;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.NotificationType;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Role;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.NotificationMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.NotificationRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.WebSocketService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.impl.NotificationServiceImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private NotificationMapper notificationMapper;

  @Mock
  private WebSocketService webSocketService;

  @Mock
  private UserRepository userRepository;

  private TestNotificationServiceImpl notificationService;

  private User user;
  private User otherUser;
  private Notification notification;
  private NotificationDTO notificationDTO;
  private List<Notification> notifications;
  private Page<Notification> notificationPage;

  @BeforeEach
  void setUp() {
    // Setup user data
    user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");

    Role userRole = new Role();
    userRole.setName("ROLE_USER");

    Set<Role> userRoles = new HashSet<>();
    userRoles.add(userRole);
    user.setRoles(userRoles);

    otherUser = new User();
    otherUser.setId(2L);
    otherUser.setUsername("otheruser");
    otherUser.setEmail("other@example.com");
    otherUser.setRoles(userRoles);

    // Setup notification
    notification = new Notification();
    notification.setId(1L);
    notification.setUser(user);
    notification.setType(NotificationType.ORDER_CONFIRMATION);
    notification.setContent("Your order has been confirmed");
    notification.setIsRead(false);

    // Setup notification DTO
    notificationDTO = new NotificationDTO();
    notificationDTO.setId(1L);
    notificationDTO.setUserId(user.getId());
    notificationDTO.setType(NotificationType.ORDER_CONFIRMATION);
    notificationDTO.setContent("Your order has been confirmed");
    notificationDTO.setIsRead(false);

    // Setup notification list and page
    notifications = new ArrayList<>();
    notifications.add(notification);
    Pageable pageable = PageRequest.of(0, 10);
    notificationPage = new PageImpl<>(notifications, pageable, notifications.size());

    // Create service with test user
    notificationService = new TestNotificationServiceImpl(
        userRepository,
        notificationRepository,
        notificationMapper,
        webSocketService,
        user
    );

    // Setup mapper behavior with lenient strictness
    Mockito.lenient().when(notificationMapper.toDto(notification)).thenReturn(notificationDTO);
  }

  @Test
  @DisplayName("Should get current user notifications")
  void shouldGetCurrentUserNotifications() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    when(notificationRepository.findByUser(user, pageable)).thenReturn(notificationPage);

    // When
    PaginationResponse<NotificationDTO> result = notificationService.getCurrentUserNotifications(
        pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(notification.getId());
    assertThat(result.totalCount()).isEqualTo(1);

    verify(notificationRepository).findByUser(user, pageable);
  }

  @Test
  @DisplayName("Should get current user unread notifications")
  void shouldGetCurrentUserUnreadNotifications() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    when(notificationRepository.findByUserAndIsReadFalse(user, pageable)).thenReturn(
        notificationPage);

    // When
    PaginationResponse<NotificationDTO> result =
        notificationService.getCurrentUserUnreadNotifications(pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(notification.getId());
    assertThat(result.data().get(0).getIsRead()).isFalse();
    assertThat(result.totalCount()).isEqualTo(1);

    verify(notificationRepository).findByUserAndIsReadFalse(user, pageable);
  }

  @Test
  @DisplayName("Should get current user notifications by type")
  void shouldGetCurrentUserNotificationsByType() {
    // Given
    Pageable pageable = PageRequest.of(0, 10);
    NotificationType type = NotificationType.ORDER_CONFIRMATION;
    when(notificationRepository.findByUserAndType(user, type, pageable)).thenReturn(
        notificationPage);

    // When
    PaginationResponse<NotificationDTO> result =
        notificationService.getCurrentUserNotificationsByType(type, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(notification.getId());
    assertThat(result.data().get(0).getType()).isEqualTo(type);
    assertThat(result.totalCount()).isEqualTo(1);

    verify(notificationRepository).findByUserAndType(user, type, pageable);
  }

  @Test
  @DisplayName("Should get notification by ID")
  void shouldGetNotificationById() {
    // Given
    when(notificationRepository.findById(notification.getId())).thenReturn(
        Optional.of(notification));

    // When
    NotificationDTO result = notificationService.getNotification(notification.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(notification.getId());
    assertThat(result.getUserId()).isEqualTo(user.getId());
    assertThat(result.getType()).isEqualTo(notification.getType());
    assertThat(result.getContent()).isEqualTo(notification.getContent());

    verify(notificationRepository).findById(notification.getId());
  }

  @Test
  @DisplayName("Should throw exception when getting non-existent notification")
  void shouldThrowExceptionWhenGettingNonExistentNotification() {
    // Given
    Long nonExistentId = 999L;
    when(notificationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> notificationService.getNotification(nonExistentId))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(notificationRepository).findById(nonExistentId);
  }

  @Test
  @DisplayName("Should throw exception when getting notification belonging to another user")
  void shouldThrowExceptionWhenGettingNotificationBelongingToAnotherUser() {
    // Given
    Notification otherUserNotification = new Notification();
    otherUserNotification.setId(2L);
    otherUserNotification.setUser(otherUser);
    otherUserNotification.setType(NotificationType.ORDER_CONFIRMATION);
    otherUserNotification.setContent("Your order has been shipped");
    otherUserNotification.setIsRead(false);

    when(notificationRepository.findById(otherUserNotification.getId())).thenReturn(
        Optional.of(otherUserNotification));

    // When/Then
    assertThatThrownBy(() -> notificationService.getNotification(otherUserNotification.getId()))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);

    verify(notificationRepository).findById(otherUserNotification.getId());
  }

  @Test
  @DisplayName("Should mark notification as read")
  void shouldMarkNotificationAsRead() {
    // Given
    when(notificationRepository.findById(notification.getId())).thenReturn(
        Optional.of(notification));
    when(notificationRepository.save(notification)).thenReturn(notification);

    // Create a DTO for the read notification
    NotificationDTO readNotificationDTO = new NotificationDTO();
    readNotificationDTO.setId(notification.getId());
    readNotificationDTO.setUserId(user.getId());
    readNotificationDTO.setType(notification.getType());
    readNotificationDTO.setContent(notification.getContent());
    readNotificationDTO.setIsRead(true);

    when(notificationMapper.toDto(notification)).thenReturn(readNotificationDTO);

    // When
    NotificationDTO result = notificationService.markAsRead(notification.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(notification.getId());
    assertThat(result.getIsRead()).isTrue();

    verify(notificationRepository).findById(notification.getId());
    verify(notificationRepository).save(notification);
    verify(webSocketService).sendNotification(eq(user.getId()), any(NotificationDTO.class));
  }

  @Test
  @DisplayName("Should throw exception when marking non-existent notification as read")
  void shouldThrowExceptionWhenMarkingNonExistentNotificationAsRead() {
    // Given
    Long nonExistentId = 999L;
    when(notificationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> notificationService.markAsRead(nonExistentId))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(notificationRepository).findById(nonExistentId);
    verify(notificationRepository, never()).save(any(Notification.class));
  }

  @Test
  @DisplayName("Should throw exception when marking notification belonging to another user as read")
  void shouldThrowExceptionWhenMarkingNotificationBelongingToAnotherUserAsRead() {
    // Given
    Notification otherUserNotification = new Notification();
    otherUserNotification.setId(2L);
    otherUserNotification.setUser(otherUser);
    otherUserNotification.setType(NotificationType.ORDER_CONFIRMATION);
    otherUserNotification.setContent("Your order has been shipped");
    otherUserNotification.setIsRead(false);

    when(notificationRepository.findById(otherUserNotification.getId())).thenReturn(
        Optional.of(otherUserNotification));

    // When/Then
    assertThatThrownBy(() -> notificationService.markAsRead(otherUserNotification.getId()))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);

    verify(notificationRepository).findById(otherUserNotification.getId());
    verify(notificationRepository, never()).save(any(Notification.class));
  }

  @Test
  @DisplayName("Should mark all notifications as read")
  void shouldMarkAllNotificationsAsRead() {
    // Given
    when(notificationRepository.findByUserAndIsReadFalse(eq(user), any(Pageable.class))).thenReturn(
        notificationPage);
    when(notificationRepository.markAllAsRead(user)).thenReturn(1);

    // When
    int result = notificationService.markAllAsRead();

    // Then
    assertThat(result).isEqualTo(1);
    verify(notificationRepository).markAllAsRead(user);
    verify(webSocketService, times(2)).sendNotification(eq(user.getId()),
                                                        any(NotificationDTO.class));
  }

  @Test
  @DisplayName("Should create notification")
  void shouldCreateNotification() {
    // Given
    when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
      Notification savedNotification = invocation.getArgument(0);
      savedNotification.setId(1L);
      return savedNotification;
    });
    when(notificationRepository.countByUserAndIsReadFalse(user)).thenReturn(1L);

    // Create a DTO for the notification
    NotificationDTO createdNotificationDTO = new NotificationDTO();
    createdNotificationDTO.setId(1L);
    createdNotificationDTO.setUserId(user.getId());
    createdNotificationDTO.setType(NotificationType.ORDER_CONFIRMATION);
    createdNotificationDTO.setContent("Your order has been confirmed");
    createdNotificationDTO.setIsRead(false);

    // Create a DTO for the count notification
    NotificationDTO countNotificationDTO = new NotificationDTO();
    countNotificationDTO.setUserId(user.getId());
    countNotificationDTO.setType(NotificationType.UNREAD_COUNT);
    countNotificationDTO.setContent("1");
    countNotificationDTO.setIsRead(true);

    // Mock the mapper to return the created notification DTO
    when(notificationMapper.toDto(any(Notification.class))).thenReturn(createdNotificationDTO);

    // When
    notificationService.createNotification(user, NotificationType.ORDER_CONFIRMATION,
                                           "Your order has been confirmed");

    // Then
    verify(notificationRepository).save(any(Notification.class));
    verify(notificationRepository).countByUserAndIsReadFalse(user);
    verify(webSocketService, times(2)).sendNotification(eq(user.getId()),
                                                        any(NotificationDTO.class));
  }

  @Test
  @DisplayName("Should count unread notifications")
  void shouldCountUnreadNotifications() {
    // Given
    when(notificationRepository.countByUserAndIsReadFalse(user)).thenReturn(5L);

    // When
    long result = notificationService.countUnreadNotifications();

    // Then
    assertThat(result).isEqualTo(5L);
    verify(notificationRepository).countByUserAndIsReadFalse(user);
  }

  // Test subclass that overrides getCurrentUser to avoid static mocking
  private class TestNotificationServiceImpl extends NotificationServiceImpl {
    private final User currentUser;

    public TestNotificationServiceImpl(
        UserRepository userRepository,
        NotificationRepository notificationRepository,
        NotificationMapper notificationMapper,
        WebSocketService webSocketService,
        User currentUser) {
      super(userRepository, notificationRepository, notificationMapper, webSocketService);
      this.currentUser = currentUser;
    }

    @Override
    protected User getCurrentUser() {
      return currentUser;
    }
  }
}
