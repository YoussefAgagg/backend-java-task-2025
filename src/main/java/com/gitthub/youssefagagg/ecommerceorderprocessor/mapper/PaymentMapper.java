package com.gitthub.youssefagagg.ecommerceorderprocessor.mapper;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaymentDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Order;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Payment;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for the entity {@link Payment} and its DTO {@link PaymentDTO}.
 */
@Mapper(componentModel = "spring",
        uses = {OrderMapper.class})
public interface PaymentMapper extends EntityMapper<PaymentDTO, Payment> {

  @Mapping(target = "orderId",
           source = "order.id")
  PaymentDTO toDto(Payment payment);

  @Mapping(target = "order.id",
           source = "orderId")
  @Mapping(target = "order",
           ignore = true)
  Payment toEntity(PaymentDTO paymentDTO);

  /**
   * Partial update of a payment entity with a DTO.
   *
   * @param payment    the entity to update
   * @param paymentDTO the DTO with updates
   */
  @Override
  @Named("partialUpdate")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "order",
           ignore = true)
  void partialUpdate(@MappingTarget Payment payment, PaymentDTO paymentDTO);

  /**
   * After mapping, set the order if orderId is provided.
   *
   * @param paymentDTO the source DTO
   * @param payment    the target entity
   */
  @AfterMapping
  default void setOrderIfIdExists(PaymentDTO paymentDTO, @MappingTarget Payment payment) {
    if (paymentDTO.getOrderId() != null && payment.getOrder() == null) {
      payment.setOrder(new Order());
      payment.getOrder().setId(paymentDTO.getOrderId());
    }
  }
}