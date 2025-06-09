package com.gitthub.youssefagagg.ecommerceorderprocessor.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gitthub.youssefagagg.ecommerceorderprocessor.TestcontainersConfiguration;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.AuditLogDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.AuditLog;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.Product;
import com.gitthub.youssefagagg.ecommerceorderprocessor.entity.User;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.AuditLogRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.ProductRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.AuditService;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class AuditServiceIT {

  @Autowired
  private AuditService auditService;

  @Autowired
  private AuditLogRepository auditLogRepository;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private UserRepository userRepository;

  private User testUser;
  private Product testProduct;
  private AuditLogDTO auditLogDTO;

  @BeforeEach
  void setUp() {
    // Clean up any existing data
    auditLogRepository.deleteAll();
    productRepository.deleteAll();

    // Create test user
    testUser = new User();
    testUser.setUsername("testuser");
    testUser.setEmail("testuser@example.com");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    // Password must be exactly 60 characters long
    testUser.setPassword("$2a$10$eDhncK/4cNH2KE.Y51AWpeL8/5TCTWBpxmVhvZuLfCPIb4SLQtEP6");
    testUser = userRepository.save(testUser);

    // Create test product
    testProduct = new Product();
    testProduct.setName("Test Product");
    testProduct.setDescription("Test Description");
    testProduct.setPrice(BigDecimal.valueOf(99.99));
    testProduct = productRepository.save(testProduct);

    // Create audit log DTO for testing
    auditLogDTO = new AuditLogDTO();
    auditLogDTO.setEntityType("Product");
    auditLogDTO.setEntityId(testProduct.getId());
    auditLogDTO.setAction("CREATE");
    auditLogDTO.setChanges("{\"name\":\"Test Product\",\"price\":99.99}");
  }

  @Test
  @DisplayName("Should create audit log asynchronously")
  void shouldCreateAuditLog() throws Exception {
    // Given
    String entityType = auditLogDTO.getEntityType();
    Long entityId = auditLogDTO.getEntityId();

    // When
    auditService.createLogAsync(entityType, entityId, testProduct);

    // Wait for async operation to complete
    TimeUnit.SECONDS.sleep(1);

    // Then
    Pageable pageable = PageRequest.of(0, 10);
    Page<AuditLog> auditLogsPage =
        auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedDateDesc(
            entityType, entityId, pageable);

    assertThat(auditLogsPage.getContent()).isNotEmpty();
    AuditLog savedAuditLog = auditLogsPage.getContent().get(0);
    assertThat(savedAuditLog.getEntityType()).isEqualTo(entityType);
    assertThat(savedAuditLog.getEntityId()).isEqualTo(entityId);
    assertThat(savedAuditLog.getAction()).isEqualTo("CREATE");
  }

  @Test
  @DisplayName("Should get audit log by ID")
  void shouldGetAuditLogById() throws Exception {
    // Given
    String entityType = auditLogDTO.getEntityType();
    Long entityId = auditLogDTO.getEntityId();

    // Create an audit log asynchronously
    auditService.createLogAsync(entityType, entityId, testProduct);

    // Wait for async operation to complete
    TimeUnit.SECONDS.sleep(1);

    // Get the created audit log
    Pageable pageable = PageRequest.of(0, 10);
    Page<AuditLog> auditLogsPage =
        auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedDateDesc(
            entityType, entityId, pageable);
    Long auditLogId = auditLogsPage.getContent().get(0).getId();

    // When
    AuditLogDTO result = auditService.getAuditLog(auditLogId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(auditLogId);
    assertThat(result.getEntityType()).isEqualTo(entityType);
    assertThat(result.getEntityId()).isEqualTo(entityId);
    assertThat(result.getAction()).isEqualTo("CREATE");
  }

  @Test
  @DisplayName("Should throw exception when getting non-existent audit log")
  void shouldThrowExceptionWhenGettingNonExistentAuditLog() {
    // Given
    Long nonExistentId = 999L;

    // When/Then
    assertThatThrownBy(() -> auditService.getAuditLog(nonExistentId))
        .isInstanceOf(CustomException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
  }

  @Test
  @DisplayName("Should get audit logs for entity")
  void shouldGetAuditLogsForEntity() throws Exception {
    // Given
    String entityType = "Product";
    Long entityId = testProduct.getId();

    // Create first audit log asynchronously (CREATE)
    auditService.createLogAsync(entityType, entityId, testProduct);

    // Wait for async operation to complete
    TimeUnit.SECONDS.sleep(1);

    // Create second audit log asynchronously (UPDATE)
    Product updatedProduct = new Product();
    updatedProduct.setId(testProduct.getId());
    updatedProduct.setName("Updated Product");
    updatedProduct.setDescription(testProduct.getDescription());
    updatedProduct.setPrice(BigDecimal.valueOf(109.99));

    auditService.updateLogAsync(entityType, entityId, null, testProduct, updatedProduct);

    // Wait for async operation to complete
    TimeUnit.SECONDS.sleep(1);

    Pageable pageable = PageRequest.of(0, 10);

    // When
    PaginationResponse<AuditLogDTO> result = auditService.getAuditLogsForEntity(entityType,
                                                                                entityId,
                                                                                pageable);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.data()).hasSize(2);
    assertThat(result.totalCount()).isEqualTo(2);

    // Verify the audit logs are for the correct entity
    assertThat(result.data()).allMatch(log ->
                                           log.getEntityType().equals("Product") &&
                                           log.getEntityId().equals(testProduct.getId())
                                      );

    // Verify we have both CREATE and UPDATE actions
    assertThat(result.data()).extracting(AuditLogDTO::getAction)
                             .containsExactlyInAnyOrder("CREATE", "UPDATE");
  }

  @Test
  @DisplayName("Should create log asynchronously")
  void shouldCreateLogAsync() throws Exception {
    // Given
    String entityType = "Product";
    Long entityId = testProduct.getId();

    // When
    auditService.createLogAsync(entityType, entityId, testProduct);

    // Wait for async operation to complete
    TimeUnit.SECONDS.sleep(1);

    // Then
    Pageable pageable = PageRequest.of(0, 10);
    Page<AuditLog> auditLogsPage =
        auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedDateDesc(
            entityType, entityId, pageable);
    assertThat(auditLogsPage.getContent()).isNotEmpty();
    AuditLog auditLog = auditLogsPage.getContent().get(0);
    assertThat(auditLog.getEntityType()).isEqualTo(entityType);
    assertThat(auditLog.getEntityId()).isEqualTo(entityId);
    assertThat(auditLog.getAction()).isEqualTo("CREATE");
  }
}
