package com.gitthub.youssefagagg.ecommerceorderprocessor.mapper;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.CreateProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.ProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for the entity {@link Product} and its DTOs {@link ProductDTO} and
 * {@link CreateProductDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper extends EntityMapper<ProductDTO, Product> {

  @Override
  ProductDTO toDto(Product product);

  @Override
  Product toEntity(ProductDTO productDTO);

  /**
   * Convert CreateProductDTO to Product entity.
   *
   * @param createProductDTO the DTO to convert
   * @return the entity
   */
  Product toEntity(CreateProductDTO createProductDTO);

  /**
   * Partial update of a product entity with a DTO.
   *
   * @param product    the entity to update
   * @param productDTO the DTO with updates
   */
  @Override
  @Named("partialUpdate")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void partialUpdate(@MappingTarget Product product, ProductDTO productDTO);

  /**
   * Partial update of a product entity with a CreateProductDTO.
   *
   * @param product          the entity to update
   * @param createProductDTO the DTO with updates
   */
  @Named("partialUpdateFromCreateDTO")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void partialUpdate(@MappingTarget Product product, CreateProductDTO createProductDTO);
}
