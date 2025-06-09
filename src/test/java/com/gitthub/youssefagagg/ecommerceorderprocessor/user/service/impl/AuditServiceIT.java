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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
  @DisplayName("Should create audit log")
  void shouldCreateAuditLog() {
    // When
    AuditLogDTO result = auditService.createAuditLog(auditLogDTO);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getEntityType()).isEqualTo(auditLogDTO.getEntityType());
    assertThat(result.getEntityId()).isEqualTo(auditLogDTO.getEntityId());
    assertThat(result.getAction()).isEqualTo(auditLogDTO.getAction());
    assertThat(result.getChanges()).isEqualTo(auditLogDTO.getChanges());

    // Verify audit log is saved in database
    AuditLog savedAuditLog = auditLogRepository.findById(result.getId()).orElseThrow();
    assertThat(savedAuditLog.getEntityType()).isEqualTo(auditLogDTO.getEntityType());
    assertThat(savedAuditLog.getEntityId()).isEqualTo(auditLogDTO.getEntityId());
    assertThat(savedAuditLog.getAction()).isEqualTo(auditLogDTO.getAction());
    assertThat(savedAuditLog.getChanges()).isEqualTo(auditLogDTO.getChanges());
  }

  @Test
  @DisplayName("Should get audit log by ID")
  void shouldGetAuditLogById() {
    // Given
    AuditLogDTO createdAuditLog = auditService.createAuditLog(auditLogDTO);

    // When
    AuditLogDTO result = auditService.getAuditLog(createdAuditLog.getId());

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(createdAuditLog.getId());
    assertThat(result.getEntityType()).isEqualTo(auditLogDTO.getEntityType());
    assertThat(result.getEntityId()).isEqualTo(auditLogDTO.getEntityId());
    assertThat(result.getAction()).isEqualTo(auditLogDTO.getAction());
    assertThat(result.getChanges()).isEqualTo(auditLogDTO.getChanges());
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
  void shouldGetAuditLogsForEntity() {
    // Given
    auditService.createAuditLog(auditLogDTO);

    // Create another audit log for the same entity
    AuditLogDTO updateAuditLogDTO = new AuditLogDTO();
    updateAuditLogDTO.setEntityType("Product");
    updateAuditLogDTO.setEntityId(testProduct.getId());
    updateAuditLogDTO.setAction("UPDATE");
    updateAuditLogDTO.setChanges("{\"name\":\"Updated Product\",\"price\":109.99}");
    auditService.createAuditLog(updateAuditLogDTO);

    Pageable pageable = PageRequest.of(0, 10);

    // When
    PaginationResponse<AuditLogDTO> result = auditService.getAuditLogsForEntity("Product",
                                                                                testProduct.getId(),
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
    List<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedDateDesc(
        entityType, entityId);
    assertThat(auditLogs).isNotEmpty();
    assertThat(auditLogs.get(0).getEntityType()).isEqualTo(entityType);
    assertThat(auditLogs.get(0).getEntityId()).isEqualTo(entityId);
    assertThat(auditLogs.get(0).getAction()).isEqualTo("CREATE");
  }
}