package com.gitthub.youssefagagg.ecommerceorderprocessor.mapper;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.OrderItemDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.OrderItem;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for the entity {@link OrderItem} and its DTO {@link OrderItemDTO}.
 */
@Mapper(componentModel = "spring",
        uses = {ProductMapper.class})
public interface OrderItemMapper extends EntityMapper<OrderItemDTO, OrderItem> {

  @Mapping(target = "orderId",
           source = "order.id")
  @Mapping(target = "productId",
           source = "product.id")
  @Mapping(target = "productName",
           source = "product.name")
  @Mapping(target = "subtotal",
           expression = "java(orderItem.getSubtotal())")
  OrderItemDTO toDto(OrderItem orderItem);

  @Mapping(target = "order.id",
           source = "orderId")
  @Mapping(target = "product.id",
           source = "productId")
  @Mapping(target = "order",
           ignore = true)
  @Mapping(target = "product",
           ignore = true)
  OrderItem toEntity(OrderItemDTO orderItemDTO);

  /**
   * Partial update of an order item entity with a DTO.
   *
   * @param orderItem    the entity to update
   * @param orderItemDTO the DTO with updates
   */
  @Override
  @Named("partialUpdate")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "order",
           ignore = true)
  @Mapping(target = "product",
           ignore = true)
  void partialUpdate(@MappingTarget OrderItem orderItem, OrderItemDTO orderItemDTO);

  /**
   * After mapping, set the order and product if their IDs are provided.
   *
   * @param orderItemDTO the source DTO
   * @param orderItem    the target entity
   */
  @AfterMapping
  default void setRelatedEntitiesIfIdsExist(OrderItemDTO orderItemDTO,
                                            @MappingTarget OrderItem orderItem) {
    if (orderItemDTO.getOrderId() != null && orderItem.getOrder() == null) {
      orderItem.setOrder(
          new Order());
      orderItem.getOrder().setId(orderItemDTO.getOrderId());
    }

    if (orderItemDTO.getProductId() != null && orderItem.getProduct() == null) {
      orderItem.setProduct(
          new Product());
      orderItem.getProduct().setId(orderItemDTO.getProductId());
    }
  }
}