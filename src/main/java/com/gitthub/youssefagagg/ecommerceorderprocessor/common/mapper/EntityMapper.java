package com.gitthub.youssefagagg.ecommerceorderprocessor.common.mapper;


import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Contract for a generic dto to entity mapper.
 *
 * @param <D> - DTO type parameter.
 * @param <E> - Entity type parameter.
 * @author Youssef Agagg
 */

public interface EntityMapper<D, E> {

  /**
   * Converts a Data Transfer Object (DTO) into its corresponding Entity.
   *
   * @param dto the Data Transfer Object to be converted
   * @return the resulting Entity after the conversion
   */
  E toEntity(D dto);

  /**
   * Converts an Entity into its corresponding Data Transfer Object (DTO).
   *
   * @param entity the Entity to be converted
   * @return the resulting Data Transfer Object after the conversion
   */
  D toDto(E entity);

  /**
   * Converts a list of Data Transfer Objects (DTOs) into their corresponding Entities.
   *
   * @param dtoList the list of Data Transfer Objects to be converted
   * @return a list of Entities resulting from the conversion
   */
  List<E> toEntities(List<D> dtoList);

  /**
   * Converts a list of Entities into their corresponding Data Transfer Objects (DTOs).
   *
   * @param entityList the list of Entities to be converted
   * @return a list of Data Transfer Objects resulting from the conversion
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  List<D> toDTOs(List<E> entityList);

  /**
   * Updates a given entity with non-null properties from the provided Data Transfer Object (DTO).
   * Only non-null properties in the DTO will be mapped to the entity.
   *
   * @param entity the target entity to be updated
   * @param dto    the Data Transfer Object containing the properties to update the entity with
   */
  @Named("partialUpdate")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void partialUpdate(@MappingTarget E entity, D dto);

}
