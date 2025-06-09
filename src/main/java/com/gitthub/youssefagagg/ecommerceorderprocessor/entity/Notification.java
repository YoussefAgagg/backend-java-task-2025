package com.gitthub.youssefagagg.ecommerceorderprocessor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Notification entity for storing notification information.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class Notification extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id",
              nullable = false)
  private User user;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "type",
          nullable = false)
  private NotificationType type;

  @NotNull
  @Size(max = 1000)
  @Column(name = "content",
          length = 1000,
          nullable = false)
  private String content;

  @NotNull
  @Column(name = "is_read",
          nullable = false)
  private Boolean isRead;

  /**
   * Mark notification as read.
   */
  public void markAsRead() {
    this.isRead = true;
  }


}