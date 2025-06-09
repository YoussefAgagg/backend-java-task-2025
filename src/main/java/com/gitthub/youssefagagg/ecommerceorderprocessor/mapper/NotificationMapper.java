package com.gitthub.youssefagagg.ecommerceorderprocessor.mapper;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.NotificationDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Notification;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for the entity {@link Notification} and its DTO {@link NotificationDTO}.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper extends EntityMapper<NotificationDTO, Notification> {

  @Mapping(target = "userId",
           source = "user.id")
  @Mapping(target = "userName",
           expression = "java(notification.getUser().getFirstName() + \" \" + notification.getUser().getLastName())")
  NotificationDTO toDto(Notification notification);

  @Mapping(target = "user.id",
           source = "userId")
  @Mapping(target = "user",
           ignore = true)
  Notification toEntity(NotificationDTO notificationDTO);

  /**
   * Partial update of a notification entity with a DTO.
   *
   * @param notification    the entity to update
   * @param notificationDTO the DTO with updates
   */
  @Override
  @Named("partialUpdate")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "user",
           ignore = true)
  void partialUpdate(@MappingTarget Notification notification, NotificationDTO notificationDTO);

  /**
   * After mapping, set the user if userId is provided.
   *
   * @param notificationDTO the source DTO
   * @param notification    the target entity
   */
  @AfterMapping
  default void setUserIfIdExists(NotificationDTO notificationDTO,
                                 @MappingTarget Notification notification) {
    if (notificationDTO.getUserId() != null && notification.getUser() == null) {
      notification.setUser(new User());
      notification.getUser().setId(notificationDTO.getUserId());
    }
  }
}