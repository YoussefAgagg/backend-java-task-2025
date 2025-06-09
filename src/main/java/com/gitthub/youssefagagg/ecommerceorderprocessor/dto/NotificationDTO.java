package com.gitthub.youssefagagg.ecommerceorderprocessor.dto;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Notification;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.NotificationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for {@link Notification}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class NotificationDTO extends AbstractAuditingEntityDto {

  private Long id;

  private Long userId;

  private String userName;

  @NotNull
  private NotificationType type;

  @NotNull
  @Size(max = 1000)
  private String content;

  @NotNull
  private Boolean isRead;
}