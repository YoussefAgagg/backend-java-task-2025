package com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper;

import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.EntityMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.AuditLogDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.AuditLog;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper for the entity {@link AuditLog} and its DTO {@link AuditLogDTO}.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper extends EntityMapper<AuditLogDTO, AuditLog> {

  AuditLogDTO toDto(AuditLog auditLog);

  AuditLog toEntity(AuditLogDTO auditLogDTO);

  /**
   * Partial update of an audit log entity with a DTO.
   *
   * @param auditLog    the entity to update
   * @param auditLogDTO the DTO with updates
   */
  @Override
  @Named("partialUpdate")
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void partialUpdate(@MappingTarget AuditLog auditLog, AuditLogDTO auditLogDTO);
}