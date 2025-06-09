package com.gitthub.youssefagagg.ecommerceorderprocessor.repository;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Notification;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.NotificationType;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Notification} entity.
 */
@Repository
public interface NotificationRepository
    extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

  /**
   * Find notifications by user.
   *
   * @param user     the user
   * @param pageable the pagination information
   * @return the list of notifications
   */
  Page<Notification> findByUser(User user, Pageable pageable);

  /**
   * Find unread notifications by user.
   *
   * @param user     the user
   * @param pageable the pagination information
   * @return the list of unread notifications
   */
  Page<Notification> findByUserAndIsReadFalse(User user, Pageable pageable);

  /**
   * Find notifications by user and type.
   *
   * @param user     the user
   * @param type     the type
   * @param pageable the pagination information
   * @return the list of notifications
   */
  Page<Notification> findByUserAndType(User user, NotificationType type, Pageable pageable);

  /**
   * Count unread notifications by user.
   *
   * @param user the user
   * @return the count of unread notifications
   */
  long countByUserAndIsReadFalse(User user);

  /**
   * Mark all notifications as read for a user.
   *
   * @param user the user
   * @return the number of notifications updated
   */
  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
  int markAllAsRead(User user);
}