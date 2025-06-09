package com.gitthub.youssefagagg.ecommerceorderprocessor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * AuditLog entity for tracking changes to entities.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AuditLog extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(max = 50)
  @Column(name = "entity_type",
          length = 50,
          nullable = false)
  private String entityType;

  @NotNull
  @Column(name = "entity_id",
          nullable = false)
  private Long entityId;

  @NotNull
  @Size(max = 50)
  @Column(name = "action",
          length = 50,
          nullable = false)
  private String action;

  @Column(name = "changes",
          columnDefinition = "TEXT")
  private String changes;

  /**
   * Create an audit log for a create action.
   *
   * @param entityType the type of entity
   * @param entityId   the ID of the entity
   * @param changes    the changes made
   * @return the audit log
   */
  public static AuditLog createLog(String entityType, Long entityId, String changes) {
    return AuditLog.builder()
                   .entityType(entityType)
                   .entityId(entityId)
                   .action("CREATE")
                   .changes(changes)
                   .build();
  }

  /**
   * Create an audit log for an update action.
   *
   * @param entityType the type of entity
   * @param entityId   the ID of the entity
   * @param changes    the changes made
   * @return the audit log
   */
  public static AuditLog updateLog(String entityType, Long entityId, String changes) {
    return AuditLog.builder()
                   .entityType(entityType)
                   .entityId(entityId)
                   .action("UPDATE")
                   .changes(changes)
                   .build();
  }

  /**
   * Create an audit log for a delete action.
   *
   * @param entityType the type of entity
   * @param entityId   the ID of the entity
   * @return the audit log
   */
  public static AuditLog deleteLog(String entityType, Long entityId) {
    return AuditLog.builder()
                   .entityType(entityType)
                   .entityId(entityId)
                   .action("DELETE")
                   .build();
  }
}