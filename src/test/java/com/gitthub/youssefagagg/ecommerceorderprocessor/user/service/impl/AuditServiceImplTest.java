package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.AuditLogDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.AuditLog;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.mapper.AuditLogMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.AuditLogRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.impl.AuditServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

  @Mock
  private AuditLogRepository auditLogRepository;

  @Mock
  private AuditLogMapper auditLogMapper;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ObjectMapper objectMapper;

  private AuditServiceImpl auditService;

  private AuditLog auditLog;
  private AuditLogDTO auditLogDTO;
  private List<AuditLog> auditLogs;
  private Page<AuditLog> auditLogPage;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    // Initialize service
    auditService = new AuditServiceImpl(
        userRepository,
        auditLogRepository,
        auditLogMapper,
        objectMapper
    );

    // Setup test data
    auditLog = new AuditLog();
    auditLog.setId(1L);
    auditLog.setEntityType("Product");
    auditLog.setEntityId(100L);
    auditLog.setAction("CREATE");
    auditLog.setChanges("{\"name\":\"Test Product\",\"price\":99.99}");

    auditLogDTO = new AuditLogDTO();
    auditLogDTO.setId(1L);
    auditLogDTO.setEntityType("Product");
    auditLogDTO.setEntityId(100L);
    auditLogDTO.setAction("CREATE");
    auditLogDTO.setChanges("{\"name\":\"Test Product\",\"price\":99.99}");

    auditLogs = new ArrayList<>();
    auditLogs.add(auditLog);

    pageable = PageRequest.of(0, 10);
    auditLogPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

    // Setup mapper behavior with lenient stubbing to avoid "unnecessary stubbing" errors
    Mockito.lenient().when(auditLogMapper.toDto(auditLog)).thenReturn(auditLogDTO);
  }

  @Test
  @DisplayName("Should get audit logs for entity")
  void shouldGetAuditLogsForEntity() {
    // Given
    String entityType = "Product";
    Long entityId = 100L;
    when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedDateDesc(entityType, entityId))
        .thenReturn(auditLogs);

    // When
    PaginationResponse<AuditLogDTO> result = auditService.getAuditLogsForEntity(entityType,
                                                                                entityId, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(auditLog.getId());
    assertThat(result.data().get(0).getEntityType()).isEqualTo(entityType);
    assertThat(result.data().get(0).getEntityId()).isEqualTo(entityId);
    assertThat(result.totalCount()).isEqualTo(1);

    verify(auditLogRepository).findByEntityTypeAndEntityIdOrderByCreatedDateDesc(entityType,
                                                                                 entityId);
  }

  @Test
  @DisplayName("Should get audit logs by entity type")
  void shouldGetAuditLogsByEntityType() {
    // Given
    String entityType = "Product";
    when(auditLogRepository.findByEntityType(entityType, pageable)).thenReturn(auditLogPage);

    // When
    PaginationResponse<AuditLogDTO> result = auditService.getAuditLogsByEntityType(entityType,
                                                                                   pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(auditLog.getId());
    assertThat(result.data().get(0).getEntityType()).isEqualTo(entityType);
    assertThat(result.totalCount()).isEqualTo(1);

    verify(auditLogRepository).findByEntityType(entityType, pageable);
  }

  @Test
  @DisplayName("Should get audit logs by action")
  void shouldGetAuditLogsByAction() {
    // Given
    String action = "CREATE";
    when(auditLogRepository.findByAction(action, pageable)).thenReturn(auditLogPage);

    // When
    PaginationResponse<AuditLogDTO> result = auditService.getAuditLogsByAction(action, pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(1);
    assertThat(result.data().get(0).getId()).isEqualTo(auditLog.getId());
    assertThat(result.data().get(0).getAction()).isEqualTo(action);
    assertThat(result.totalCount()).isEqualTo(1);

    verify(auditLogRepository).findByAction(action, pageable);
  }

  @Test
  @DisplayName("Should get audit log by ID")
  void shouldGetAuditLogById() {
    // Given
    Long id = 1L;
    when(auditLogRepository.findById(id)).thenReturn(Optional.of(auditLog));

    // When
    AuditLogDTO result = auditService.getAuditLog(id);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(auditLog.getId());
    assertThat(result.getEntityType()).isEqualTo(auditLog.getEntityType());
    assertThat(result.getEntityId()).isEqualTo(auditLog.getEntityId());
    assertThat(result.getAction()).isEqualTo(auditLog.getAction());
    assertThat(result.getChanges()).isEqualTo(auditLog.getChanges());

    verify(auditLogRepository).findById(id);
  }

  @Test
  @DisplayName("Should throw exception when getting non-existent audit log")
  void shouldThrowExceptionWhenGettingNonExistentAuditLog() {
    // Given
    Long nonExistentId = 999L;
    when(auditLogRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> auditService.getAuditLog(nonExistentId))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);

    verify(auditLogRepository).findById(nonExistentId);
  }

  @Test
  @DisplayName("Should create audit log")
  void shouldCreateAuditLog() {
    // Given
    when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
    when(auditLogMapper.toEntity(auditLogDTO)).thenReturn(auditLog);

    // When
    AuditLogDTO result = auditService.createAuditLog(auditLogDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(auditLog.getId());
    assertThat(result.getEntityType()).isEqualTo(auditLog.getEntityType());
    assertThat(result.getEntityId()).isEqualTo(auditLog.getEntityId());
    assertThat(result.getAction()).isEqualTo(auditLog.getAction());
    assertThat(result.getChanges()).isEqualTo(auditLog.getChanges());

    verify(auditLogMapper).toEntity(auditLogDTO);
    verify(auditLogRepository).save(auditLog);
  }

  @Test
  @DisplayName("Should create log asynchronously")
  void shouldCreateLogAsync() throws Exception {
    // Given
    String entityType = "Product";
    Long entityId = 100L;
    Object entity = new Object();
    String jsonEntity = "{\"name\":\"Test Product\",\"price\":99.99}";

    when(objectMapper.writeValueAsString(entity)).thenReturn(jsonEntity);
    when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

    // When
    auditService.createLogAsync(entityType, entityId, entity);

    // Then
    verify(objectMapper).writeValueAsString(entity);
    verify(auditLogRepository).save(any(AuditLog.class));
  }

  @Test
  @DisplayName("Should handle exception when creating log asynchronously")
  void shouldHandleExceptionWhenCreatingLogAsync() throws Exception {
    // Given
    String entityType = "Product";
    Long entityId = 100L;
    Object entity = new Object();

    when(objectMapper.writeValueAsString(entity)).thenThrow(new RuntimeException("Test exception"));

    // When
    auditService.createLogAsync(entityType, entityId, entity);

    // Then
    verify(objectMapper).writeValueAsString(entity);
    verify(auditLogRepository, never()).save(any(AuditLog.class));
  }

  @Test
  @DisplayName("Should update log asynchronously with message and values")
  void shouldUpdateLogAsyncWithMessageAndValues() throws Exception {
    // Given
    String entityType = "Product";
    Long entityId = 100L;
    String message = "Product updated";
    Object oldValue = new Object();
    Object newValue = new Object();
    String oldJson = "{\"name\":\"Old Product\",\"price\":89.99}";
    String newJson = "{\"name\":\"New Product\",\"price\":99.99}";

    when(objectMapper.writeValueAsString(oldValue)).thenReturn(oldJson);
    when(objectMapper.writeValueAsString(newValue)).thenReturn(newJson);
    when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

    // When
    auditService.updateLogAsync(entityType, entityId, message, oldValue, newValue);

    // Then
    verify(objectMapper).writeValueAsString(oldValue);
    verify(objectMapper).writeValueAsString(newValue);
    verify(auditLogRepository).save(any(AuditLog.class));
  }

  @Test
  @DisplayName("Should update log asynchronously with only message")
  void shouldUpdateLogAsyncWithOnlyMessage() throws Exception {
    // Given
    String entityType = "Product";
    Long entityId = 100L;
    String message = "Product updated";

    when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

    // When
    auditService.updateLogAsync(entityType, entityId, message, null, null);

    // Then
    verify(objectMapper, never()).writeValueAsString(any());
    verify(auditLogRepository).save(any(AuditLog.class));
  }

  @Test
  @DisplayName("Should delete log asynchronously")
  void shouldDeleteLogAsync() {
    // Given
    String entityType = "Product";
    Long entityId = 100L;
    when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

    // When
    auditService.deleteLogAsync(entityType, entityId);

    // Then
    verify(auditLogRepository).save(any(AuditLog.class));
  }
}
