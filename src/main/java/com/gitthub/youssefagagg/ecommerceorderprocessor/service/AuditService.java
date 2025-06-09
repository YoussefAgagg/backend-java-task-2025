package com.gitthub.youssefagagg.ecommerceorderprocessor.service;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.AuditLogDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.AuditLog;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing
 * {@link AuditLog}.
 */
public interface AuditService {

  /**
   * Get all audit logs for an entity with pagination.
   *
   * @param entityType the entity type
   * @param entityId   the entity ID
   * @return the list of audit logs
   */
  PaginationResponse<AuditLogDTO> getAuditLogsForEntity(String entityType, Long entityId,
                                                        Pageable pageable);

  /**
   * Get all audit logs for an entity type with pagination.
   *
   * @param entityType the entity type
   * @param pageable   the pagination information
   * @return the list of audit logs
   */
  PaginationResponse<AuditLogDTO> getAuditLogsByEntityType(String entityType, Pageable pageable);

  /**
   * Get all audit logs for an action with pagination.
   *
   * @param action   the action
   * @param pageable the pagination information
   * @return the list of audit logs
   */
  PaginationResponse<AuditLogDTO> getAuditLogsByAction(String action, Pageable pageable);

  /**
   * Get an audit log by ID.
   *
   * @param id the audit log ID
   * @return the audit log
   */
  AuditLogDTO getAuditLog(Long id);

  /**
   * Create an audit log.
   *
   * @param auditLogDTO the audit log to create
   * @return the created audit log
   */
  AuditLogDTO createAuditLog(AuditLogDTO auditLogDTO);

  /**
   * Asynchronously create an audit log for a create action. The object will be converted to JSON
   * internally.
   *
   * @param entityType the type of entity
   * @param entityId   the ID of the entity
   * @param entity     the entity object to be converted to JSON
   */
  void createLogAsync(String entityType, Long entityId, Object entity);

  /**
   * Asynchronously create an audit log for an update action. The objects will be converted to JSON
   * internally.
   *
   * @param entityType the type of entity
   * @param entityId   the ID of the entity
   * @param message    optional message to include (can be null)
   * @param oldValue   the old value of the entity (can be null if only providing a message)
   * @param newValue   the new value of the entity (can be null if only providing a message)
   */
  void updateLogAsync(String entityType, Long entityId, String message, Object oldValue,
                      Object newValue);

  /**
   * Asynchronously create an audit log for a delete action.
   *
   * @param entityType the type of entity
   * @param entityId   the ID of the entity
   */
  void deleteLogAsync(String entityType, Long entityId);
}
