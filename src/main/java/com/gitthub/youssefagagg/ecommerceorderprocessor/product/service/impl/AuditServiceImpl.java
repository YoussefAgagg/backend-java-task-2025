package com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.AuditLogDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.AuditLog;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper.AuditLogMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.AuditLogRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.AuditService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.BaseService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link AuditLog}.
 */
@Service
@Slf4j
public class AuditServiceImpl extends BaseService implements AuditService {

  private final AuditLogRepository auditLogRepository;
  private final AuditLogMapper auditLogMapper;
  private final ObjectMapper objectMapper;

  public AuditServiceImpl(
      UserRepository userRepository,
      AuditLogRepository auditLogRepository,
      AuditLogMapper auditLogMapper,
      ObjectMapper objectMapper) {
    super(userRepository);
    this.auditLogRepository = auditLogRepository;
    this.auditLogMapper = auditLogMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse<AuditLogDTO> getAuditLogsForEntity(String entityType, Long entityId,
                                                               Pageable pageable) {
    log.debug("Request to get AuditLogs for entity type: {} and entity ID: {}", entityType,
              entityId);

    List<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedDateDesc(
        entityType, entityId);

    // Create a page from the list
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), auditLogs.size());
    List<AuditLog> pageContent = auditLogs.subList(start, end);
    Page<AuditLog> page = new PageImpl<>(pageContent, pageable, auditLogs.size());

    Page<AuditLogDTO> dtoPage = page.map(auditLogMapper::toDto);

    return createPaginationResponse(dtoPage);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse<AuditLogDTO> getAuditLogsByEntityType(String entityType,
                                                                  Pageable pageable) {
    log.debug("Request to get AuditLogs by entity type: {}", entityType);

    Page<AuditLog> result = auditLogRepository.findByEntityType(entityType, pageable);
    Page<AuditLogDTO> dtoPage = result.map(auditLogMapper::toDto);

    return createPaginationResponse(dtoPage);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse<AuditLogDTO> getAuditLogsByAction(String action, Pageable pageable) {
    log.debug("Request to get AuditLogs by action: {}", action);

    Page<AuditLog> result = auditLogRepository.findByAction(action, pageable);
    Page<AuditLogDTO> dtoPage = result.map(auditLogMapper::toDto);

    return createPaginationResponse(dtoPage);
  }

  @Override
  @Transactional(readOnly = true)
  public AuditLogDTO getAuditLog(Long id) {
    log.debug("Request to get AuditLog : {}", id);

    return auditLogRepository.findById(id)
                             .map(auditLogMapper::toDto)
                             .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                                                                    "AuditLog not found"));
  }

  @Override
  @Transactional
  public AuditLogDTO createAuditLog(AuditLogDTO auditLogDTO) {
    log.debug("Request to create AuditLog : {}", auditLogDTO);

    AuditLog auditLog = auditLogMapper.toEntity(auditLogDTO);
    auditLog = auditLogRepository.save(auditLog);

    return auditLogMapper.toDto(auditLog);
  }

  @Override
  @Async("taskExecutor")
  @Transactional
  public void createLogAsync(String entityType, Long entityId, Object entity) {
    log.debug("Request to asynchronously create audit log for entity type: {} and entity ID: {}",
              entityType, entityId);
    try {
      String jsonEntity = objectMapper.writeValueAsString(entity);
      AuditLog auditLog = AuditLog.createLog(entityType, entityId, jsonEntity);
      auditLogRepository.save(auditLog);
    } catch (Exception e) {
      log.error("Error creating audit log", e);
    }
  }

  @Override
  @Async("taskExecutor")
  @Transactional
  public void updateLogAsync(String entityType, Long entityId, String message, Object oldValue,
                             Object newValue) {
    log.debug(
        "Request to asynchronously create update audit log for entity type: {} and entity ID: {}",
        entityType, entityId);
    try {
      String changes;
      if (message != null && !message.isBlank()) {
        changes = message;
        if (oldValue != null && newValue != null) {
          String oldJson = objectMapper.writeValueAsString(oldValue);
          String newJson = objectMapper.writeValueAsString(newValue);
          changes += ", Old: " + oldJson + ", New: " + newJson;
        }
      } else if (oldValue != null && newValue != null) {
        String oldJson = objectMapper.writeValueAsString(oldValue);
        String newJson = objectMapper.writeValueAsString(newValue);
        changes = "Old: " + oldJson + ", New: " + newJson;
      } else {
        changes = "Update performed";
      }

      AuditLog auditLog = AuditLog.updateLog(entityType, entityId, changes);
      auditLogRepository.save(auditLog);
    } catch (Exception e) {
      log.error("Error creating update audit log", e);
    }
  }

  @Override
  @Async("taskExecutor")
  @Transactional
  public void deleteLogAsync(String entityType, Long entityId) {
    log.debug(
        "Request to asynchronously create delete audit log for entity type: {} and entity ID: {}",
        entityType, entityId);
    try {
      AuditLog auditLog = AuditLog.deleteLog(entityType, entityId);
      auditLogRepository.save(auditLog);
    } catch (Exception e) {
      log.error("Error creating delete audit log", e);
    }
  }

}
