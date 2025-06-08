package com.gitthub.youssefagagg.ecommerceorderprocessor.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base abstract class for entities which will hold definitions for created, last modified, created
 * by, last modified by attributes.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdBy", "createdDate", "lastModifiedBy",
    "lastModifiedDate"},
                      allowGetters = true)
@Getter
@Setter
@ToString
public abstract class AbstractAuditingEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @CreatedBy
  @Column(name = "created_by",
          nullable = false,
          updatable = false)
  private String createdBy;

  @CreatedDate
  @Column(name = "created_date",
          updatable = false)
  private Instant createdDate = Instant.now();

  @LastModifiedBy
  @Column(name = "last_modified_by")
  private String lastModifiedBy;

  @LastModifiedDate
  @Column(name = "last_modified_date")
  private Instant lastModifiedDate = Instant.now();

  @Version
  @Column(name = "version")
  private Integer version = 0;
}
