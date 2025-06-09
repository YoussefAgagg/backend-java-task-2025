package com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper;

import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.EntityMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.InventoryDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Inventory;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for the entity {@link Inventory} and its DTO {@link InventoryDTO}.
 */
@Mapper(componentModel = "spring",
        uses = {ProductMapper.class})
public interface InventoryMapper extends EntityMapper<InventoryDTO, Inventory> {

  @Mapping(target = "productId",
           source = "product.id")
  @Mapping(target = "productName",
           source = "product.name")
  @Mapping(target = "availableQuantity",
           expression = "java(inventory.getAvailableQuantity())")
  InventoryDTO toDto(Inventory inventory);

  @Mapping(target = "product.id",
           source = "productId")
  @Mapping(target = "product",
           ignore = true)
  Inventory toEntity(InventoryDTO inventoryDTO);

  /**
   * Partial update of an inventory entity with a DTO.
   *
   * @param inventory    the entity to update
   * @param inventoryDTO the DTO with updates
   */
  @Override
  @Named("partialUpdate")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "product",
           ignore = true)
  void partialUpdate(@MappingTarget Inventory inventory, InventoryDTO inventoryDTO);

  /**
   * After mapping, set the product if productId is provided.
   *
   * @param inventoryDTO the source DTO
   * @param inventory    the target entity
   */
  @AfterMapping
  default void setProductIfIdExists(InventoryDTO inventoryDTO, @MappingTarget Inventory inventory) {
    if (inventoryDTO.getProductId() != null && inventory.getProduct() == null) {
      inventory.setProduct(
          new com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Product());
      inventory.getProduct().setId(inventoryDTO.getProductId());
    }
  }
}