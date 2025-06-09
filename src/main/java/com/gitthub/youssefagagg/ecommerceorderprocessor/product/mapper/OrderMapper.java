package com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.EntityMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.OrderDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Order;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for the entity {@link Order} and its DTO {@link OrderDTO}.
 */
@Mapper(componentModel = "spring",
        uses = {OrderItemMapper.class})
public interface OrderMapper extends EntityMapper<OrderDTO, Order> {

  @Mapping(target = "userId",
           source = "user.id")
  @Mapping(target = "userName",
           expression = "java(order.getUser().getFirstName() + \" \" + order.getUser().getLastName())")
  OrderDTO toDto(Order order);

  @Mapping(target = "user.id",
           source = "userId")
  @Mapping(target = "user",
           ignore = true)
  Order toEntity(OrderDTO orderDTO);

  /**
   * Partial update of an order entity with a DTO.
   *
   * @param order    the entity to update
   * @param orderDTO the DTO with updates
   */
  @Override
  @Named("partialUpdate")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "user",
           ignore = true)
  @Mapping(target = "orderItems",
           ignore = true)
  void partialUpdate(@MappingTarget Order order, OrderDTO orderDTO);

  /**
   * After mapping, set the user if userId is provided.
   *
   * @param orderDTO the source DTO
   * @param order    the target entity
   */
  @AfterMapping
  default void setUserIfIdExists(OrderDTO orderDTO, @MappingTarget Order order) {
    if (orderDTO.getUserId() != null && order.getUser() == null) {
      order.setUser(new User());
      order.getUser().setId(orderDTO.getUserId());
    }
  }
}