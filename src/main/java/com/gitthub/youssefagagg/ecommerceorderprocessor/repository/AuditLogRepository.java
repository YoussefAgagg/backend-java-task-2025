package com.gitthub.youssefagagg.ecommerceorderprocessor.repository;

import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.AuditLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link AuditLog} entity.
 */
@Repository
public interface AuditLogRepository
    extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

  /**
   * Find audit logs by entity type and entity ID.
   *
   * @param entityType the entity type
   * @param entityId   the entity ID
   * @return the list of audit logs
   */
  List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedDateDesc(String entityType,
                                                                   Long entityId);

  /**
   * Find audit logs by entity type.
   *
   * @param entityType the entity type
   * @param pageable   the pagination information
   * @return the list of audit logs
   */
  Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

  /**
   * Find audit logs by action.
   *
   * @param action   the action
   * @param pageable the pagination information
   * @return the list of audit logs
   */
  Page<AuditLog> findByAction(String action, Pageable pageable);
}