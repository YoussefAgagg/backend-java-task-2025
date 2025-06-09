package com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.impl;

import com.gitthub.youssefagagg.ecommerceorderprocessor.dto.PaginationResponse;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.CreateProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.InventoryDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.dto.ProductDTO;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Inventory;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.entity.Product;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper.InventoryMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.mapper.ProductMapper;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.InventoryRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.repository.ProductRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.AuditService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.product.service.ProductService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.repository.UserRepository;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.BaseService;
import com.gitthub.youssefagagg.ecommerceorderprocessor.service.WebSocketService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Product}.
 */
@Service
@Slf4j
public class ProductServiceImpl extends BaseService implements ProductService {

  private final ProductRepository productRepository;
  private final InventoryRepository inventoryRepository;
  private final ProductMapper productMapper;
  private final InventoryMapper inventoryMapper;
  private final WebSocketService webSocketService;
  private final AuditService auditService;

  public ProductServiceImpl(
      UserRepository userRepository,
      ProductRepository productRepository,
      InventoryRepository inventoryRepository,
      ProductMapper productMapper,
      InventoryMapper inventoryMapper,
      WebSocketService webSocketService,
      AuditService auditService) {
    super(userRepository);
    this.productRepository = productRepository;
    this.inventoryRepository = inventoryRepository;
    this.productMapper = productMapper;
    this.inventoryMapper = inventoryMapper;
    this.webSocketService = webSocketService;
    this.auditService = auditService;
  }

  @Override
  @Transactional
  public ProductDTO save(CreateProductDTO createProductDTO) {
    log.debug("Request to save Product : {}", createProductDTO);

    Product product = productMapper.toEntity(createProductDTO);
    product = productRepository.save(product);

    // Create initial inventory with the specified quantity
    Inventory inventory = Inventory.builder()
                                   .product(product)
                                   .quantity(createProductDTO.getQuantity())
                                   .reservedQuantity(0)
                                   .build();
    inventoryRepository.save(inventory);

    auditService.createLogAsync("Product", product.getId(), product);
    auditService.createLogAsync("Inventory", inventory.getId(), inventory);

    ProductDTO result = productMapper.toDto(product);
    result.setAvailableQuantity(createProductDTO.getQuantity());
    return result;
  }

  @Override
  @Transactional
  public ProductDTO update(CreateProductDTO createProductDTO) {
    log.debug("Request to update Product : {}", createProductDTO);

    if (createProductDTO.getId() == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST, "ID must not be null");
    }

    // Check if product exists
    Product existingProduct = productRepository.findById(createProductDTO.getId())
                                               .orElseThrow(() -> new CustomException(
                                                   ErrorCode.ENTITY_NOT_FOUND,
                                                   "Product not found"));

    // Clone the existing product to preserve old values
    Product oldProduct = existingProduct.cloneObject();

    // Update the product
    productMapper.partialUpdate(existingProduct, createProductDTO);
    Product updatedProduct = productRepository.save(existingProduct);

    // Update inventory if provided
    Inventory inventory = updatedProduct.getInventory();

    // Clone the existing inventory to preserve old values
    Inventory oldInventory = inventory.cloneObject();

    // Update inventory quantity
    inventory.setQuantity(createProductDTO.getQuantity());
    inventoryRepository.save(inventory);

    // Create audit logs asynchronously
    auditService.updateLogAsync("Product", updatedProduct.getId(), null, oldProduct,
                                updatedProduct);
    auditService.updateLogAsync("Inventory", inventory.getId(), null, oldInventory, inventory);

    ProductDTO result = productMapper.toDto(updatedProduct);
    result.setAvailableQuantity(inventory.getAvailableQuantity());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse<ProductDTO> findAll(Pageable pageable) {
    log.debug("Request to get all Products");
    Page<Product> result = productRepository.findAll(pageable);
    Page<ProductDTO> dtoPage = result.map(product -> {
      ProductDTO productDTO = productMapper.toDto(product);
      // Add inventory information to the DTO
      productDTO.setAvailableQuantity(product.getInventory().getAvailableQuantity());
      return productDTO;
    });
    return createPaginationResponse(dtoPage);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse<ProductDTO> findByNameContaining(String name, Pageable pageable) {
    log.debug("Request to get all Products containing name: {}", name);
    Page<Product> result = productRepository.findByNameContainingIgnoreCase(name, pageable);
    Page<ProductDTO> dtoPage = result.map(product -> {
      ProductDTO productDTO = productMapper.toDto(product);
      // Add inventory information to the DTO
      productDTO.setAvailableQuantity(product.getInventory().getAvailableQuantity());
      return productDTO;
    });

    return createPaginationResponse(dtoPage);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductDTO findOne(Long id) {
    log.debug("Request to get Product : {}", id);
    return productRepository.findById(id)
                            .map(product -> {
                              ProductDTO productDTO = productMapper.toDto(product);
                              // Add inventory information to the DTO
                              productDTO.setAvailableQuantity(
                                  product.getInventory().getAvailableQuantity());
                              return productDTO;
                            })
                            .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                                                                   "Product not found"));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.debug("Request to delete Product : {}", id);

    // Check if product exists
    Product product = productRepository.findById(id)
                                       .orElseThrow(
                                           () -> new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                                                                     "Product not found"));

    // Check if product has inventory
    Inventory inventory = product.getInventory();
    if (inventory.getQuantity() > 0) {
      throw new CustomException(ErrorCode.OPTIMISTIC_LOCKING_ERROR,
                                "Cannot delete product with inventory");
    }

    // Delete inventory
    inventoryRepository.delete(inventory);

    // Delete product
    productRepository.deleteById(id);

    // Create audit log asynchronously
    auditService.deleteLogAsync("Product", id);
    auditService.deleteLogAsync("Inventory", inventory.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public InventoryDTO getInventory(Long productId) {
    log.debug("Request to get Inventory for Product : {}", productId);

    // Check if product exists
    Product product = productRepository.findById(productId)
                                       .orElseThrow(
                                           () -> new CustomException(ErrorCode.ENTITY_NOT_FOUND,
                                                                     "Product not found"));
    return inventoryMapper.toDto(product.getInventory());
  }

  @Override
  @Transactional
  public InventoryDTO updateInventory(InventoryDTO inventoryDTO) {
    log.debug("Request to update Inventory : {}", inventoryDTO);

    if (inventoryDTO.getId() == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST,
                                "Either ID or Product ID must be provided");
    }

    Inventory inventory = inventoryRepository.findById(inventoryDTO.getId())
                                             .orElseThrow(
                                                 () -> new CustomException(
                                                     ErrorCode.ENTITY_NOT_FOUND,
                                                     "Inventory not found"));

    // Clone the existing inventory to preserve old values
    Inventory oldInventory = inventory.cloneObject();

    // Update the inventory
    inventoryMapper.partialUpdate(inventory, inventoryDTO);
    Inventory updatedInventory = inventoryRepository.save(inventory);

    // Create audit log asynchronously
    auditService.updateLogAsync("Inventory", updatedInventory.getId(), null, oldInventory,
                                updatedInventory);

    // Convert to DTO for response
    InventoryDTO updatedInventoryDTO = inventoryMapper.toDto(updatedInventory);

    // Send real-time inventory update to all connected clients
    webSocketService.sendInventoryUpdate(updatedInventoryDTO);

    // Check if inventory is low and send alert to admin dashboard
    if (updatedInventory.getAvailableQuantity() < 5) {
      webSocketService.sendLowStockAlert(updatedInventoryDTO);
    }

    return updatedInventoryDTO;
  }


  @Transactional(readOnly = true)
  @Override
  public List<InventoryDTO> getLowStockAlerts(int threshold) {
    log.debug("Service request to get low stock alerts with threshold: {}", threshold);

    return inventoryRepository.findLowStock(threshold).stream()
                              .map(inventory -> {
                                InventoryDTO dto = new InventoryDTO();
                                dto.setId(inventory.getId());
                                dto.setProductId(inventory.getProduct().getId());
                                dto.setProductName(inventory.getProduct().getName());
                                dto.setQuantity(inventory.getQuantity());
                                dto.setReservedQuantity(inventory.getReservedQuantity());
                                dto.setAvailableQuantity(inventory.getAvailableQuantity());
                                return dto;
                              })
                              .toList();
  }
}
